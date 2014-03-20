(ns corenlp.util
  "Various utility functions that make NLP life easier"

  (:import
   (edu.stanford.nlp.util
    StringUtils)))

(defn ngrams
  ([n tokens]
     (StringUtils/getNgrams tokens n n))
  ([min-n max-n tokens]
     (StringUtils/getNgrams tokens min-n max-n)))

(defn bigrams [tokens]
  (StringUtils/getNgrams tokens 2 2))

(defn trigrams [tokens]
  (StringUtils/getNgrams tokens 3 3))
