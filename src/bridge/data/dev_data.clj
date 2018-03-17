(ns bridge.data.dev-data
  (:require [bridge.person.data :as person.data]
            [datomic.api :as d]
            [integrant.core :as ig]))

(defmethod ig/init-key :datomic/dev-data [_ {{:datomic/keys [conn]} :datomic}]

  ;; schema
  @(d/transact conn person.data/schema)

  ;; test people
  (when (nil? (d/entity (d/db conn) [:person/email "test@cb.org"]))
    (->> [(person.data/new-person-tx {:person/email    "test@cb.org"
                                      :person/password "secret"})]
         (d/transact conn)
         deref)))
