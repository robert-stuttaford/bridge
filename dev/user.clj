(ns user
  (:require [bridge.config :as config]
            [clojure.spec.alpha :as s]
            [integrant.repl :as ig.repl]
            [expound.alpha :as expound]))

(alter-var-root (var s/*explain-out*) (constantly expound/printer))

(s/check-asserts true)

(ig.repl/set-prep! config/system)
