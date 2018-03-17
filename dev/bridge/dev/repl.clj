(ns bridge.dev.repl
  (:require [bridge.config :as config]
            [datomic.api :as d]))

;; uses Datomic Peer library

(defn uri []
  (get-in (config/system) [:datomic/connection :uri]))

(defn conn []
  (d/connect (uri)))

(defn db []
  (d/db (conn)))
