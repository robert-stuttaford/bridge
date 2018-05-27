.PHONY: all test dev

all: dev

dev:
	source bridge.env && clojure -Adev script/dev.clj

serve:
	source bridge.env && clojure -m bridge.service

test:
	clojure -A\:test

clean:
	rm -rf resources/js target bridge.jar

compile:
	clojure -m cljs.main -co "{:source-map \"resources/js/app.js.map\"}" -d resources/js/out -t browser -O advanced -o resources/js/app.js -c bridge.main

uberjar:
	clojure -A\:uberjar

outdated:
	clojure -Aoutdated -a outdated

serve-jar:
	source bridge.env && java -jar bridge.jar -m bridge.service

peer-server:
	source bridge.env && bash script/datomic-peer-server.sh

pack: clean compile uberjar