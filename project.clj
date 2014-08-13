(defproject me.arnaudsj/corenlp "3.4"
  :description "Clojure wrapper for the Stanford CoreNLP tools."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.4"]
                 [edu.stanford.nlp/stanford-corenlp "3.4"
                  :classifier "models"]
                 [cc.artifice/loom "0.1.3"]]
  :plugins [[lein-exec "0.3.2"]]
  :url "https://github.com/arnaudsj/stanford-corenlp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
         :url "https://github.com/arnaudsj/stanford-corenlp"})
