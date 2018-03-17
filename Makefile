.PHONY: all test

all: figwheel

serve:
	source bridge.env && clojure -m bridge.service

test:
	clojure -A\:test

figwheel:
	source bridge.env && clojure -A\:dev dev/figwheel.clj

clean:
	rm -rf resources/js target bridge.jar

compile:
	clojure -m cljs.main -cf "{:source-map \"resources/js/app.js.map\"}" -d resources/js/out -t browser -O advanced -o resources/js/app.js -c bridge.main

uberjar:
	clojure -A\:uberjar

serve-jar:
	source bridge.env && java -jar bridge.jar -m bridge.service

pack: clean compile uberjar