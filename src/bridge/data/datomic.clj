(ns bridge.data.datomic
  (:require [datomic.api :as d]
            [datomic.client.api :as dc]
            [integrant.core :as ig]))

(defmulti init-datomic-conn! :datomic/mode)

(defmethod init-datomic-conn! :default [config]
  (throw (ex-info ":datomic/mode value node supported" config)))

(defmethod init-datomic-conn! :peer [{:keys [uri]}]
  (d/create-database uri)
  {:datomic/conn (d/connect uri)})

(defmethod init-datomic-conn! :client [{:keys [client db-name]}]
  (let [client (dc/client client)]
    {:datomic/client client
     :datomic/conn   (dc/connect client {:db-name db-name})}))

(defmethod ig/init-key :datomic/connection [_ config]
  (merge (select-keys config [:datomic/mode])
         (init-datomic-conn! config)))

(defn db [{:datomic/keys [mode conn]}]
  (case mode
    :peer   (d/db conn)
    :client (dc/db conn)))

(defn wrap-datomic [handler datomic]
  (fn [request]
    (handler (merge request datomic {:datomic/db (db datomic)}))))
