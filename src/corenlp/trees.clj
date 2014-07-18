(ns corenlp.trees
  "Provides utilities for dealing with CoreNLP trees and bindings for
  the tregex/tsurgeon tree manipulation tools."

  (:import
   (java.io StringReader)

   (edu.stanford.nlp.trees
    DiskTreebank LabeledScoredTreeFactory PennTreeReader PennTreeReaderFactory
    Tree TreebankLanguagePack)
   (edu.stanford.nlp.trees.tregex
    TregexMatcher TregexPattern)))

(defn load-treebank
  "Load a PTB-style treebank from the given path. Returns a CoreNLP
  `Treebank` object."

  ([path]
     (load-treebank path TreebankLanguagePack/DEFAULT_ENCODING))
  ([path encoding]
     (let [treebank (DiskTreebank. (PennTreeReaderFactory.) encoding)]
       (.loadPath treebank path)
       treebank)))

(defn tree-from-string
  "Parse a `Tree` object from the given PTB-style tree string. Returns
  `nil` on parse failure."
  [^java.lang.String tree-str]
  (let [reader (PennTreeReader. (StringReader. tree-str)
                                (LabeledScoredTreeFactory.))]
    (try (.readTree reader)
         (catch java.io.IOException e
           nil))))

(defn tregex-pattern
  "Returns an instance of `TregexPattern`, for use, e.g. in
  `tregex-matcher`."
  [^java.lang.String pattern-str]
  (TregexPattern/compile pattern-str))

(defmulti tregex-matcher
  "Returns an instance of `TregexMatcher`, for use, e.g. in
  `tregex-find`."
  (fn [p _] (class p)))

(defmethod tregex-matcher java.lang.String
  [^String pattern-str ^Tree tree]
  (.matcher (tregex-pattern pattern-str) tree))

(defmethod tregex-matcher TregexPattern
  [^TregexPattern tregex-pattern ^Tree tree]
  (.matcher tregex-pattern tree))

(defn tregex-match
  "Returns a map of the named nodes from the most recent match/find. If
  there are no named nodes, returns the tree node that matches the root
  node of the associated pattern. If there are named nodes, the root
  match is included in the returned map under the key `:root`."
  [^TregexMatcher matcher]
  (let [matched-root (.getMatch matcher)
        names (.getNodeNames matcher)]
    (if (empty? names)
      matched-root
      (let [named-nodes
            (apply hash-map (mapcat #(vector % (.getNode matcher %)) names))]
        (assoc named-nodes :root matched-root)))))

(defn tregex-seq
  "Returns a lazy sequence of successive matches of `pattern` in `tree`,
  where each match is produced by `tregex-match`."
  [pattern ^Tree tree]
  (let [m (tregex-matcher pattern tree)]
    ((fn step []
       (when (.find m)
         (cons (tregex-match m) (lazy-seq (step))))))))

(defn tregex-find
  "Returns the next tregex match, if any, as a map of named nodes. The
  tree node that matches the root node of the tregex pattern is stored
  with the key `:root` in the returned map."
  ([^TregexMatcher matcher]
     (when (.find matcher)
       (tregex-match matcher)))
  ([tregex-pattern ^Tree tree]
     (tregex-find (tregex-matcher tregex-pattern tree))))
