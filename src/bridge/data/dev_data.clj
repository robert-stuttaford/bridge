(ns bridge.data.dev-data
  (:require [bridge.data.datomic :as datomic]
            [bridge.person.data :as person.data]
            [integrant.core :as ig]))

(defn add-person! [conn {:person/keys [email] :as new-person}]
  (when (nil? (person.data/person-id-by-email (datomic/db conn) email))
    (let [new-person-tx (person.data/new-person-tx new-person
                                                   #:person{:name     "Test Name"
                                                            :email    "test@cb.org"
                                                            :password "secret"})]
      (person.data/save-new-person! conn new-person-tx)
      (person.data/confirm-email! conn
                                  (person.data/person-id-by-email (datomic/db conn) email)
                                  (:person/email-confirm-token new-person-tx)))))

(defmethod ig/init-key :datomic/dev-data [_ {{:datomic/keys [mode conn]} :datomic}]
  (datomic/with-datomic-mode mode
    ;; schema
    (datomic/transact! conn person.data/schema)

    ;; test people
    (add-person! conn #:person{:name     "Test Name"
                               :email    "test@cb.org"
                               :password "secret"})))
