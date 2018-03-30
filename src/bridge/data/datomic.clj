(ns bridge.data.datomic
  (:require [datomic.api :as d]
            [datomic.client.api :as dc]
            [integrant.core :as ig]))

(require 'bridge.data.datomic.spec)

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

(declare db)

(def ^:dynamic *DATOMIC-MODE* nil)

(defmacro with-datomic-mode [mode & body]
  `(binding [*DATOMIC-MODE* ~mode]
     ~@body))

(defn wrap-datomic [handler {:datomic/keys [mode conn] :as datomic}]
  (fn [request]
    (with-datomic-mode mode
      (handler (merge request datomic {:datomic/db (db conn)})))))

(defn db [conn]
  (case *DATOMIC-MODE*
    :peer   (d/db conn)
    :client (dc/db conn)))

(defn transact! [conn tx]
  (case *DATOMIC-MODE*
    :peer   @(d/transact conn tx)
    :client (dc/transact conn {:tx-data tx})))

(defn with [conn tx]
  (case *DATOMIC-MODE*
    :peer   (d/with (d/db conn) tx)
    :client (dc/with (dc/with-db conn) {:tx-data tx})))

(defn entid [db id]
  (case *DATOMIC-MODE*
    :peer   (d/entid db id)
    :client (:db/id (dc/pull db [:db/id] id))))

(defn q [& args]
  (case *DATOMIC-MODE*
    :peer   (apply d/q args)
    :client (apply dc/q args)))

(defn pull [db pattern id]
  (case *DATOMIC-MODE*
    :peer   (d/pull db pattern id)
    :client (dc/pull db pattern id)))

(defn attr [db id attr-name]
  (case *DATOMIC-MODE*
    :peer   (attr-name (d/entity db id))
    :client (attr-name (dc/pull db [attr-name] id))))

(defn attr-type [db attr-name]
  (attr db attr-name :db/valueType))

(defn attr-is-unique? [db attr-name]
  (some? (attr db attr-name :db/unique)))

(defn attr-is-ref? [db attr-name]
  (= (attr-type db attr-name) :db.type/ref))
