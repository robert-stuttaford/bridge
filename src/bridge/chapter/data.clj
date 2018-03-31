(ns bridge.chapter.data
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.slug :as slug]
            [clojure.spec.alpha :as s]))

(require 'bridge.chapter.spec)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queries

(defn chapter-id-by-slug [db slug]
  (datomic/entid db [:chapter/slug slug]))

(defn person-is-organiser? [db chapter-id person-id]
  (not (empty? (datomic/q '[:find ?chapter ?person :in $ ?chapter ?person :where
                            [?chapter :chapter/organisers ?person]]
                          db chapter-id person-id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Authorisations

(defn check-chapter-organiser [db chapter-id active-person-id]
  (when-not (person-is-organiser? db chapter-id active-person-id)
    {:error :bridge.error/not-chapter-organiser}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transactions

(defn new-chapter-tx [organiser-id
                      {:chapter/keys [title]
                       :as chapter}]
  (-> (s/assert :bridge/new-chapter chapter)
      (merge {:chapter/slug       (slug/->slug title)
              :chapter/status     :status/active
              :chapter/organisers [{:db/id organiser-id}]})))

(s/fdef new-chapter-tx
        :args (s/cat :active-person-id :bridge.datomic/id
                     :new-chapter :bridge/new-chapter)
        :ret :bridge/new-chapter-tx)

(defn save-new-chapter! [conn chapter-tx]
  (datomic/transact! conn [chapter-tx]))