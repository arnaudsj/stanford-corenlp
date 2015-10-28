(defproject me.arnaudsj/corenlp "3.5.2"
  :description "Clojure wrapper for the Stanford CoreNLP tools."
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.5.2"]
                 [edu.stanford.nlp/stanford-corenlp "3.5.2" :classifier "models"]
                 [aysylu/loom  "0.5.4"]]
  :plugins [[lein-exec "0.3.5"]]
  :url "https://github.com/arnaudsj/stanford-corenlp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
         :url "https://github.com/arnaudsj/stanford-corenlp"})
