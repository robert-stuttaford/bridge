.PHONY: all test

all: figwheel

serve:
	clojure -m bridge.service

test:
	clojure -A\:test

figwheel:
	clojure -A\:dev dev/figwheel.clj

clean:
	rm -rf resources/js target bridge.jar

compile:
	clojure -m cljs.main -d resources/js/out -t browser -O advanced -o resources/js/app.js -c bridge.main

uberjar:
	clojure -A\:uberjar

serve-jar:
	java -jar bridge.jar -m bridge.service

pack: clean compile uberjar