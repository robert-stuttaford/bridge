(ns bridge.dev.repl
  (:require [bridge.config :as config]
            [bridge.data.datomic :as datomic]
            [integrant.core :as ig]))

;; uses Datomic Peer library

(defn init-datomic []
  (ig/init-key :datomic/connection
               (:datomic/connection (config/system))))

(defn set-datomic-mode! [mode]
  (alter-var-root datomic/*DATOMIC-MODE* (constantly mode)))

(defn conn []
  (when (nil? datomic/*DATOMIC-MODE*)
    (throw (ex-info (str "Set `bridge.data.datomic/*DATOMIC-MODE*` first."
                         "You can use `bridge.dev.repl/set-datomic-mode!` "
                         "to do so at the repl.")
                    {})))
  (:datomic/conn (init-datomic)))

(defn db []
  (datomic/db (conn)))
