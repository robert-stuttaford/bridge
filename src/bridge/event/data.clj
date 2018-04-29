(ns bridge.event.data
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            [clojure.spec.alpha :as s])
  (:import java.util.UUID))

(require 'bridge.event.spec)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queries

(defn event-id-by-slug [db slug]
  (datomic/entid db [:event/slug slug]))

(defn person-is-organiser? [db event-id person-id]
  (not (empty? (datomic/q '[:find ?event ?person :in $ ?event ?person :where
                            [?event :event/organisers ?person]]
                          db event-id person-id))))

(defn event-ids-by-chapter [db chapter-id]
  (mapv first (datomic/q '[:find ?event :in $ ?chapter :where
                           [?event :event/chapter ?chapter]]
                         db chapter-id)))

(def event-for-listing-pull-spec
  [:event/id
   :event/title
   :event/slug
   :event/status
   {:event/chapter [:chapter/slug]}
   {:event/organisers [:person/name]}
   :event/start-date
   :event/end-date
   :event/registration-close-date])

(defn event-for-listing [db event-id]
  (datomic/pull db event-for-listing-pull-spec event-id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Authorisations

(defn check-event-organiser [db event-id active-person-id]
  (when-not (person-is-organiser? db event-id active-person-id)
    {:error :bridge.error/not-event-organiser}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transactions

(defn new-event-tx [chapter-id organiser-id
                    {:event/keys [title registration-close-date start-date]
                     :as event}]
  (-> (s/assert :bridge/new-event event)
      (merge {:event/id         (UUID/randomUUID)
              :event/slug       (slug/->slug title)
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
