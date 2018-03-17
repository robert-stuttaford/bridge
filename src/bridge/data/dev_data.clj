(ns bridge.data.dev-data
  (:require [bridge.data.datomic :as datomic]
            [bridge.person.data :as person.data]
            [integrant.core :as ig]))

(defmethod ig/init-key :datomic/dev-data [_ {{:datomic/keys [mode conn]} :datomic}]
  (datomic/with-datomic-mode mode
    ;; schema
    (datomic/transact! conn person.data/schema)

    ;; test people
    (when (nil? (datomic/entid (datomic/db conn)
                               [:person/email "test@cb.org"]))
      (->> [(person.data/new-person-tx
             #:person{:name     "Test Name"
                      :email    "test@cb.org"
                      :password "secret"})]
           (datomic/transact! conn)))))
