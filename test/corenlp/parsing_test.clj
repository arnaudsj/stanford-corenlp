(ns corenlp.parsing-test
  (:use clojure.test)
  (:require [corenlp]))



(deftest tokenize-test
  "Simple tokenization"
  (is (= ["Mary" "had" "a" "little" "lamb"]
         (map str (corenlp/tokenize "Mary had a little lamb")))))


(deftest postag-test
  "Simple part of speech tagging"
  (is (= '("JJ" "JJ" "NNS" "VBP" "RB" ".")
         (corenlp/pos-tag "Colorless green ideas sleep furiously."))))


(deftest lemma-test
  "Lemmatization"
  (is (= '("the" "child" "have" "stinky" "foot" ".")
         (corenlp/lemmatize "The child had stinky feet."))))


(deftest named-entities-test
  "Named Entity Recognition"
  (is (= '("PERSON" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "LOCATION" "O")
         (corenlp/named-entities "Barack Obama is the president of the United States."))))
