# corenlp

Clojure wrapper for Stanford CoreNLP tools.  Currently very incomplete, only
wraps the tokenizer, parser and dependencies. Welcoming pull requests!

[![Build Status](https://travis-ci.org/arnaudsj/stanford-corenlp.svg)](https://travis-ci.org/arnaudsj/stanford-corenlp)

## Usage

### Tokenization

    (use 'corenlp)
    (def text "This is a simple sentence.")
    (tokenize text)

### Part-of-Speech Tagging

    (use 'corenlp)
    (pos-tag "Colorless green ideas sleep furiously.")
    ;; => ("JJ" "JJ" "NNS" "VBP" "RB" ".")

### Lemmatization

    (use 'corenlp)
    (lemmatize "The men have four cars.")
    ;; => ("the" "man" "have" "four" "car" ".")

### Named-Entity Recognition

    (use 'corenlp)
    (named-entities "Barack Obama is the president of the United States.")
    ;; => ("PERSON" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "LOCATION" "O")

### Parsing

To parse a sentence:

	(use 'corenlp)
	(parse (tokenize text))

You will get back a LabeledScoredTreeNode which you can plug in to
other Stanford CoreNLP functions or can convert to a standard Treebank
string with:

	(str (parse (tokenize text)))

### Stanford Dependencies

	(dependency-graph (dependency-parse (tokenize "I like cheese.")))

will parse the sentence and return the dependency graph as a
[loom](https://github.com/jkk/loom) graph, which you can then traverse with
standard graph algorithms like shortest path, etc. You can also view it:

	(def graph (dependency-graph "I like cheese."))
	(use 'loom.io)
	(view graph)

This requires GraphViz to be installed.

## License

Copyright (C) 2011-2015 Contributors (Clojure code only)

Distributed under the Eclipse Public License, the same as Clojure.

## Contributors

- Cory Giles
- Hans Engel
- Sébastien Arnaud
