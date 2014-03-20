(ns corenlp.classify
  "Wrapper around CoreNLP's classifier library. Support is focused on
  the commonly used linear classifier."

  (:import
   (edu.stanford.nlp.ling
    BasicDatum)
   (edu.stanford.nlp.classify
    Dataset
    LinearClassifierFactory LinearClassifier)))

(defmulti make-classifier
  "Train a classifier of the given type with the given dataset.

   - `instances` is an ordered collection of feature vectors (strings)
   - `labels` is an ordered collection of labels corresponding to each
     of the feature vectors"
  #(first %&))

(defmethod make-classifier :linear [_ instances labels]
  (let [dataset (Dataset. (count instances))

        ;; TODO support arguments
        factory (LinearClassifierFactory.)]

    ;; Construct dataset
    (doseq [[features label] (map vector instances labels)]
      (.add dataset (BasicDatum. features label)))

    (.trainClassifier factory dataset)))

(defn classify
  "Classify a feature vector given a trained probabilistic classifier.
  Returns the string label of the highest-probability class."
  [classifier features]

  (let [datum (BasicDatum. features)]
    (.classOf classifier datum)))

(defn classify-probabilities
  "Return the probabilities of each class given a feature vector and a
  trained probabilistic classifier.

  Returns a map from class label (string) to probability."
  [classifier features]

  (let [class-counter (.probabilityOf classifier (BasicDatum. features))]
    (reduce #(assoc %1 (.getKey %2) (.getValue %2))
            {}
            (.entrySet class-counter))))
