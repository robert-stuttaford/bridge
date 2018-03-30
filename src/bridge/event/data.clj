(ns bridge.event.data
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            [clojure.spec.alpha :as s]))

(require 'bridge.event.spec)

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Authorisations

(defn check-event-organiser [db event-id active-user-id]
  (when-not (person-is-organiser? db event-id active-user-id)
    {:error :bridge.error/not-event-organiser}))

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

(s/fdef new-event-tx
        :args (s/cat :chapter-id :bridge.datomic/id
                     :organiser-id :bridge.datomic/id
                     :new-event :bridge/new-event)
        :ret :bridge/new-event-tx)

(defn save-new-event! [conn event-tx]
  (datomic/transact! conn [event-tx]))

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
