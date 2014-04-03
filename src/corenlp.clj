(ns corenlp
  (:import
    (java.io StringReader)
    (java.util Properties)
    (edu.stanford.nlp.process
      DocumentPreprocessor Morphology PTBTokenizer)
    (edu.stanford.nlp.ling
      Word CoreAnnotations$LemmaAnnotation
      CoreAnnotations$NamedEntityTagAnnotation
      CoreAnnotations$PartOfSpeechAnnotation)
    (edu.stanford.nlp.tagger.maxent MaxentTagger)
    (edu.stanford.nlp.trees 
      LabeledScoredTreeNode PennTreebankLanguagePack  
      LabeledScoredTreeReaderFactory)
    (edu.stanford.nlp.parser.lexparser
      LexicalizedParser)
    (edu.stanford.nlp.ie NERClassifierCombiner)
    (edu.stanford.nlp.ie.regexp
      NumberSequenceClassifier))

  (:use
    (loom graph attr)
    clojure.set)
  (:gen-class :main true))

;;;;;;;;;;;;;;;;
;; Preprocessing
;;;;;;;;;;;;;;;;

(defn tokenize [s]
  "Tokenize an input string into a sequence of Word objects."
  (.tokenize
    (PTBTokenizer/newPTBTokenizer
     (StringReader. s))))

(defn tokenize-core-label [s]
  "Tokenize an input string into a sequence of `CoreLabel` objects."
  (.tokenize
    (PTBTokenizer/newPTBTokenizer
     (StringReader. s)
     false ; tokenize newlines
     false ; invertible
     )))

(defn split-sentences [text]
  "Split a string into a sequence of sentences, each of which is a sequence of Word objects. (Thus, this method both splits sentences and tokenizes simultaneously.)"
  (let [rdr (StringReader. text)]
    (map #(vec (map str %))
      (iterator-seq
        (.iterator
          (DocumentPreprocessor. rdr))))))

(defmulti word
  "Attempt to convert a given object into a Word, which is used by many downstream algorithms."
  type)

(defmethod word String [s]
  (Word. s))

(defmethod word Word [w] w)

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Part-of-speech tagging
;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^{:private true} 
  load-pos-tagger
  (memoize (fn [] (MaxentTagger. MaxentTagger/DEFAULT_JAR_PATH))))

(defmulti pos-tag
  "Tag a sequence of words with their parts of speech, returning a sequence of TaggedWord objects."
  type)

(defn pos-tag
  "Tag a sequence of words with their parts of speech, returning a sequence of
`TaggedWord` objects."
  [coll]
  (.tagSentence (load-pos-tagger)
                (java.util.ArrayList. (map word coll))))

(defn pos-tag-annotate
  "Annotate a `CoreLabel` sequence with part-of-speech tags."
  [coll]

  (doall (map #(.set %1 CoreAnnotations$PartOfSpeechAnnotation (.tag %2))
            coll (.tagSentence (load-pos-tagger) coll)))
  coll)

;;;;;;;;;;;;;;;;
;; Lemmatization
;;;;;;;;;;;;;;;;

(def ^{:private true}
  morphology (Morphology.))

(defn lemmatize-word
  "Lemmatize a single word object."
  [word]
  (let [tag (.tag word)]
    (cond
     (empty? tag) (.stem morphology word)
     ;; TODO phrasal verb check
     :else (.lemma morphology (.word word) tag))))

(defn lemmatize
  "Lemmatize a sequence of `TaggedWord` objects with their parts of speech."
  [sentence]
  (map lemmatize-word sentence))

(defn lemma-annotate
  "Annotate a sequence of `CoreLabel` objects with their lemma."
  [sentence]
  (doall (map #(.set %1 CoreAnnotations$LemmaAnnotation
                   (lemmatize-word %1))
              sentence))
  sentence)

;;;;;;
;; NER
;;;;;;

(def ^{:private true}
  ner-load-paths
  ["edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz"
   "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz"
   "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz"])

(def ^{:private true}
  load-ner-classifier
  (memoize (fn [] (NERClassifierCombiner.
                   true              ; applyNumericClassifiers
                   true              ; useSUTime
                   (into-array String ner-load-paths)))))

(def ^{:private true}
  load-numeric-classifier
  (memoize (fn [] (NumberSequenceClassifier.
                   true              ; useSUTime
                   ))))

(defn named-entities-annotate
  "Annotate named entities in a sequence of words. Returns a sequence of
  annotated `CoreLabel' objects."
  [sentence]
  (doall
      (map-indexed
       #(.setNER (.get sentence %1)
                 (.get %2 CoreAnnotations$NamedEntityTagAnnotation))
       (.classifySentence (load-ner-classifier) sentence)))
  sentence)

(defn numeric-entities-annotate
  "Annotate numeric entities in a sequence of words. Returns a sequence of
  annotated `CoreLabel` objects."
  [sentence]
  (.classifySentence (load-numeric-classifier) sentence))

;; TODO: Resolve some asymmetry of API. Most convenience functions
;; accept already-tokenized input; this one can't because it needs
;; CoreLabels (not just any tokenized input)
(defn named-entities
  "Convenience function which returns a sequence of named entity tags
  associated with each token in the string input."
  [string]

  (let [annotated (-> string tokenize-core-label pos-tag-annotate
                      lemma-annotate named-entities-annotate)]
    (map #(.ner %) annotated)))

;;;;;;;;;;
;; Parsing
;;;;;;;;;;

(let [trf (LabeledScoredTreeReaderFactory.)]

 (defn read-parse-tree [s]
   "Read a parse tree in PTB format from a string (produced by this or another parser)"
   (.readTree
    (.newTreeReader trf
                    (StringReader. s))))
 (defn read-scored-parse-tree [s]
   "Read a parse tree in PTB format with scores from a string."
   (read-parse-tree
    (->>
     (filter #(not (and
                    (.startsWith % "[")
                    (.endsWith % "]")))
             (.split s " "))
     (interpose " ")
     (apply str)))))

(def ^{:private true} load-parser
  (memoize
    (fn []
      (LexicalizedParser/loadModel))))

(defn parse [coll]
  "Use the LexicalizedParser to produce a constituent parse of sequence of strings or CoreNLP Word objects."
  (.apply (load-parser)
          (java.util.ArrayList. 
            (map word coll)))) 

;; Typed Dependencies

(defrecord DependencyParse [words tags edges])

(defn roots [dp]
  (difference
   (set (range (count (:words dp))))
   (set (map second (:edges dp)))))

(defn add-roots [dp]
  "Add explicit ROOT relations to the dependency parse. This will turn it from a polytree to a tree."
  ;;Note to self: in the new version of the parser, but not the
  ;;CoreNLP, this is already done. So when incorporating CoreNLP
  ;;updates be sure this isn't redundant.
  (assoc dp :edges
         (concat (:edges dp)
          (for [r (roots dp)]
            [-1 r :root]))))

(defn dependency-graph [dp]
  "Produce a loom graph from a DependencyParse record."
  (let [[words tags edges] (map #(% dp) [:words :tags :edges])
        g (apply digraph (map (partial take 2) edges))]
    (reduce (fn [g [i t]] (add-attr g i :tag t))
            (reduce (fn [g [i w]] (add-attr g i :word w))
                    (reduce (fn [g [gov dep type]]
                              (add-attr g gov dep :type type)) g edges)
                    (map-indexed vector words))
            (map-indexed vector tags))))

(def dependency-parse nil)

(defmulti dependency-parse 
  "Produce a DependencyParse from a sentence, which is a directed graph structure whose nodes are words and edges are typed dependencies (Marneffe et al, 2005) between them." 
  class)

(let [tlp (PennTreebankLanguagePack.)
      gsf (.grammaticalStructureFactory tlp)]

 (defmethod dependency-parse LabeledScoredTreeNode [n]
   (try
     (let [ty (.taggedYield n)]
       (DependencyParse.
        (vec (map #(.word %) ty))
        (vec (map #(.tag %) ty))
        (map (fn [d] 
               [(dec (.. d gov index))
                (dec (.. d dep index))
                (keyword
                 (.. d reln toString))])
             (.typedDependencies
              (.newGrammaticalStructure gsf n)))))
     (catch java.lang.RuntimeException _))))

(defmethod dependency-parse :default [s]
  (dependency-parse (parse s)))
