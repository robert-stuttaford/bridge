(ns bridge.data.dev-data
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.chapter.schema :as chapter.schema]
            [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            [bridge.event.schema :as event.schema]
            [bridge.person.data :as person.data]
            [bridge.person.schema :as person.schema]
            [integrant.core :as ig]))

(defn add-chapter! [conn organiser-id {:chapter/keys [title] :as new-chapter}]
  (when (nil? (chapter.data/chapter-id-by-slug (datomic/db conn) (slug/->slug title)))
    (->> (chapter.data/new-chapter-tx organiser-id new-chapter)
         (chapter.data/save-new-chapter! conn))))

(defn add-person! [conn {:person/keys [email] :as new-person}]
  (when (nil? (person.data/person-id-by-email (datomic/db conn) email))
    (let [new-person-tx (person.data/new-person-tx new-person)]
      (person.data/save-new-person! conn new-person-tx)
      (person.data/confirm-email! conn
                                  (person.data/person-id-by-email (datomic/db conn) email)
                                  (:person/confirm-email-token new-person-tx)))))

(defmethod ig/init-key :datomic/dev-data [_ {{:datomic/keys [mode conn]} :datomic}]
  (datomic/with-datomic-mode mode
    ;; people
    (datomic/transact! conn person.schema/schema)
    (add-person! conn #:person{:name     "Test Name"
                               :email    "test@cb.org"
                               :password "secret"})

    ;; chapters
    (datomic/transact! conn chapter.schema/schema)
    (add-chapter! conn [:person/email "test@cb.org"]
                  #:chapter{:title    "ClojureBridge Hermanus"
                            :location "Hermanus"})

    ;; events
    (datomic/transact! conn event.schema/schema)

    ))
