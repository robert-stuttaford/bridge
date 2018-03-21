(ns bridge.event.data
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queries

(defn event-id-by-slug [db slug]
  (datomic/entid db [:event/slug slug]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transactions

(defn new-event-tx [active-person-id
                    {:event/keys [title registration-close-date start-date]
                     :as event}]
  (-> (s/assert :bridge/new-event event)
      (merge {:event/slug       (slug/->slug title)
              :event/status     :status/draft
              :event/organisers [{:db/id active-person-id}]})
      (cond-> (nil? registration-close-date)
        (assoc :event/registration-close-date start-date))))

(defn save-new-event! [conn event-tx]
  (datomic/transact! conn [event-tx]))

(comment

  (orchestra.spec.test/instrument)

  (new-event-tx [:person/email "test@cb.org"]
                #:event{:title      "April Event"
                        :start-date #inst "2018-04-06"
                        :end-date   #inst "2018-04-07"})
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Schema

(def schema
  [{:db/ident       :event/title
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :event/slug
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :event/status
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword}

   {:db/ident       :event/organisers
    :db/cardinality :db.cardinality/many
    :db/valueType   :db.type/ref
    :db/doc         "ref to :person"}

   {:db/ident       :event/start-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/end-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/registration-close-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/details-markdown
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :event/notes-markdown
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specs

(s/def :event/status #{:status/draft :status/published :status/in-progress
                       :status/cancelled :status/complete})
(s/def :event/title :bridge.spec/required-string)
(s/def :event/slug :bridge.spec/slug)
(s/def :event/organisers (s/coll-of :bridge.datomic/ref :min-count 1))
(s/def :event/start-date inst?)
(s/def :event/end-date inst?)
(s/def :event/registration-close-date inst?)
(s/def :event/details-markdown :bridge.spec/optional-string)
(s/def :event/notes-markdown :bridge.spec/optional-string)

(s/def :bridge/new-event
  (s/keys :req [:event/title :event/start-date :event/end-date]
          :opt [:event/registration-close-date]))

(s/def :bridge/event
  (s/merge :bridge/new-event
           (s/keys :req [:event/status :event/slug :event/registration-close-date
                         :event/organisers]
                   :opt [:event/details-markdown :event/notes-markdown])))

(s/def :bridge/new-event-tx :bridge/event)

(s/fdef new-event-tx
        :args (s/cat :active-user-id :bridge.datomic/id
                     :new-event :bridge/new-event)
        :ret :bridge/new-event-tx)
