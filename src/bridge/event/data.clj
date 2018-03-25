(ns bridge.event.data
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            bridge.spec
            [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queries

(defn event-id-by-slug [db slug]
  (datomic/entid db [:event/slug slug]))

(defn person-is-organiser? [db event-id person-id]
  (not (empty? (datomic/q '[:find ?event ?person :in $ ?event ?person :where
                            [?event :event/organisers ?person]]
                          db event-id person-id))))

(defn event-ids-by-organiser [db organiser-id]
  (mapv first (datomic/q '[:find ?event :in $ ?organiser :where
                           [?event :event/organiser ?organiser]]
                         db organiser-id)))

(def event-for-editing-pull-spec
  [:event/title
   :event/slug
   :event/status
   {:event/chapter [:chapter/slug]}
   {:event/organisers [:person/name]}
   :event/start-date
   :event/end-date
   :event/registration-close-date
   :event/details-markdown
   :event/notes-markdown])

(defn event-for-editing [db event-id]
  (datomic/pull db event-for-editing-pull-spec event-id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Authorisations

(defn check-event-organiser [db event-id active-user-id]
  (when-not (person-is-organiser? db event-id active-user-id)
    {:error :bridge/not-event-organiser}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transactions

(defn new-event-tx [chapter-id organiser-id
                    {:event/keys [title registration-close-date start-date]
                     :as event}]
  (-> (s/assert :bridge/new-event event)
      (merge {:event/slug       (slug/->slug title)
              :event/status     :status/draft
              :event/chapter    chapter-id
              :event/organisers [{:db/id organiser-id}]})
      (cond-> (nil? registration-close-date)
        (assoc :event/registration-close-date start-date))))

(defn save-new-event! [conn event-tx]
  (datomic/transact! conn [event-tx]))

(comment

  (orchestra.spec.test/instrument)

  (require '[bridge.dev.repl :as repl])

  (->> (new-event-tx [:chapter/slug "clojurebridge-hermanus"]
                     [:person/email "test@cb.org"]
                     #:event{:title      "April Event"
                             :start-date #inst "2018-04-06"
                             :end-date   #inst "2018-04-07"})
       (save-new-event! (repl/conn)))

  (->> (event-id-by-slug (repl/db) "april-event")
       (event-for-editing (repl/db)))
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

   {:db/ident       :event/chapter
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/ref
    :db/doc         "ref to :chapter"}

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
(s/def :event/chapter :bridge.datomic/ref)
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
                         :event/chapter :event/organisers]
                   :opt [:event/details-markdown :event/notes-markdown])))

(s/def :bridge/new-event-tx :bridge/event)

(s/fdef new-event-tx
        :args (s/cat :chapter-id :bridge.datomic/id
                     :organiser-id :bridge.datomic/id
                     :new-event :bridge/new-event)
        :ret :bridge/new-event-tx)
