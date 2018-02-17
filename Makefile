.PHONY: all test

all: figwheel

serve:
	clojure -m bridge.service

test:
	clojure -A\:test

# java -jar uberjar.jar -m bridge.service
pack:
	clojure -A\:pack

figwheel:
	clojure -A\:dev dev/figwheel.clj

compile:
	clojure -m cljs.main -d resources/js/out -t browser -v -O advanced -o resources/js/app.js -c bridge.main
