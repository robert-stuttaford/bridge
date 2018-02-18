(ns bridge.data.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]))

(defmethod ig/init-key :datomic/connection [_ {:keys [uri]}]
  (d/create-database uri)
  (d/connect uri))

(defn wrap-datomic [handler conn]
  (fn [request]
    (handler (merge request
                    {:datomic/conn conn
                     :datomic/db (d/db conn)}))))