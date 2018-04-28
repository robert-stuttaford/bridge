(ns bridge.dev.repl
  (:require [bridge.config :as config]
            [bridge.data.datomic :as datomic]
            [integrant.core :as ig]))

(defn set-datomic-mode! [mode]
  (alter-var-root (var datomic/*DATOMIC-MODE*) (constantly mode)))

(defn conn []
  (when (nil? datomic/*DATOMIC-MODE*)
    (throw (ex-info (str "Set `bridge.data.datomic/*DATOMIC-MODE*` first."
                         "You can use `bridge.dev.repl/set-datomic-mode!` "
                         "to do so at the repl.")
                    {})))
  (-> (ig/init-key :datomic/connection
                   (-> (:datomic/connection (config/system))
                       (assoc :datomic/mode datomic/*DATOMIC-MODE*)))
      :datomic/conn))

(defn db []
  (datomic/db (conn)))

(comment

  (set-datomic-mode! :client)
  (set-datomic-mode! :peer)
  )
