(ns bridge.data.dev-data
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.chapter.schema :as chapter.schema]
            [bridge.data.datomic :as datomic]
            [bridge.data.slug :as data.slug]
            [bridge.event.api :as event.api]
            [bridge.event.data :as event.data]
            [bridge.event.schema :as event.schema]
            [bridge.person.data :as person.data]
            [bridge.person.schema :as person.schema]
            [bridge.web.api.base :as api.base]
            [integrant.core :as ig]))

(comment

  (do
   (require '[bridge.dev.repl :as repl])

   (repl/set-datomic-mode! :peer)

   ;; be sure to `cider-reset` after this!
   (datomic.api/delete-database (get-in (bridge.config/system)
                                        [:datomic/connection :uri])))

  (ig/init-key :datomic/dev-data
               {:datomic #:datomic{:mode :peer
                                   :conn (repl/conn)}})
  )

(defn add-chapter! [conn organiser-id {:chapter/keys [title] :as new-chapter}]
  (when (nil? (chapter.data/chapter-id-by-slug (datomic/db conn)
                                               (data.slug/->slug title)))
    (->> (chapter.data/new-chapter-tx organiser-id new-chapter)
         (chapter.data/save-new-chapter! conn))))

(defn add-person! [conn {:person/keys [email] :as new-person}]
  (when (nil? (person.data/person-id-by-email (datomic/db conn) email))
    (let [new-person-tx (person.data/new-person-tx new-person)]
      (person.data/save-new-person! conn new-person-tx)
      (person.data/confirm-email! conn
                                  (person.data/person-id-by-email (datomic/db conn) email)
                                  (:person/confirm-email-token new-person-tx)))))

(defn add-event! [conn chapter-id organiser-id {:event/keys [title] :as new-event}]
  (when (nil? (event.data/event-id-by-slug (datomic/db conn) (data.slug/->slug title)))
    (api.base/api {:datomic/db       (datomic/db conn)
                   :datomic/conn     conn
                   :action           ::event.api/save-new-event!
                   :chapter-id       chapter-id
                   :active-person-id organiser-id
                   :new-event        new-event})))

(defmethod ig/init-key :datomic/dev-data [_ {{:datomic/keys [mode conn]} :datomic}]
  (datomic/with-datomic-mode mode
    ;; people
    (datomic/transact! conn person.schema/schema)
    (add-person! conn #:person{:name     "Robert"
                               :email    "test@cb.org"
                               :password "secret"})

    ;; chapters
    (datomic/transact! conn chapter.schema/schema)
    (add-chapter! conn [:person/email "test@cb.org"]
                  #:chapter{:title    "ClojureBridge Hermanus"
                            :location "Hermanus"})

    ;; events
    (datomic/transact! conn event.schema/schema)

    (add-event! conn [:chapter/slug "clojurebridge-hermanus"]
                [:person/email "test@cb.org"]
                #:event{:title      "ClojureBridge April"
                        :start-date #inst "2018-04-12"
                        :end-date   #inst "2018-04-14"})

    (datomic/transact! conn [[:db/add [:event/slug "clojurebridge-april"]
                              :event/status :status/complete]])

    (add-event! conn [:chapter/slug "clojurebridge-hermanus"]
                [:person/email "test@cb.org"]
                #:event{:title      "ClojureBridge June"
                        :start-date #inst "2018-06-14"
                        :end-date   #inst "2018-06-16"})))
