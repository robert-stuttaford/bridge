(ns user
  (:require [bridge.config :as config]
            [integrant.repl :as ig.repl]))

(ig.repl/set-prep! config/system)
