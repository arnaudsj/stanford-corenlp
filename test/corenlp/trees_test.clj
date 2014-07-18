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

        ;; Again, with no named nodes
        matcher2 (trees/tregex-matcher "B >: A" tree)

        nodeA tree
        nodeB (.firstChild nodeA)]

    (is (= {:root nodeB, "A" nodeA, "B" nodeB}
           (trees/tregex-find matcher)))

    ;; When we search with no named nodes, we should just get the
    ;; matched root back
    (is (= nodeB (trees/tregex-find matcher2)))))

(deftest tregex-seq
  (let [tree (trees/tree-from-string "(A (B C))")

        nodeA tree
        nodeB (.firstChild nodeA)
        nodeC (.firstChild nodeB)]
    (is (= [nodeA nodeB nodeC] (trees/tregex-seq "__" tree)))))
