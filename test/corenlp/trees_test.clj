(ns corenlp.trees-test
  (:use clojure.test)
  (:require [corenlp.trees :as trees]))

(deftest tree-from-string
  (is (= "(A (B C))" (str (trees/tree-from-string "(A (B C))"))))

  (is (nil? (trees/tree-from-string "(A (B C)")))
  (is (nil? (trees/tree-from-string "("))))

(deftest tregex-find
  (let [tree (trees/tree-from-string "(A (B C))")
        matcher (trees/tregex-matcher "B=B >: A=A" tree)

        nodeA tree
        nodeB (.firstChild nodeA)]
    (is (= {:root nodeB, "A" nodeA, "B" nodeB}
           (trees/tregex-find matcher)))))
