.PHONY: all test

all: repl

repl:
	clojure -A\:cljs\:dev build.clj repl

# uses rlwrap
figwheel:
	clj -A\:cljs\:dev build.clj figwheel

compile:
	clojure -A\:cljs build.clj compile

test:
	clojure -A\:test

pack:
	clojure -A\:pack
