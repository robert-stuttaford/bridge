(ns bridge.data.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]))

(defn wrap-datomic [handler conn]
  (fn [request]
    (handler (merge request
                    {:datomic/conn conn
                     :datomic/db (d/db conn)}))))

(defmethod ig/init-key :datomic/connection [_ {:keys [uri]}]
  (d/create-database uri)
  (d/connect uri))

