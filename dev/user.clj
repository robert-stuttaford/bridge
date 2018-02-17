(ns user
  (:require [integrant.core :as ig]
            [integrant.repl :as ig.repl]))

(ig.repl/set-prep! #(-> "system.edn" slurp ig/read-string))
