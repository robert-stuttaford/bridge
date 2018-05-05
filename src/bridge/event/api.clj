(ns bridge.event.api
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.data.datomic :as datomic]
            [bridge.data.edit :as data.edit]
            [bridge.event.data :as event.data]
            [bridge.event.data.edit :as event.data.edit]
            [bridge.web.api.base :as api.base]
            [clojure.spec.alpha :as s]))

(require 'bridge.data.datomic.spec
         'bridge.data.edit.spec)

(s/def :bridge.api/chapter-id :bridge.datomic/lookup-ref)
(s/def :bridge.api/event-id :bridge.datomic/lookup-ref)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; List events for chapter

(defmethod api.base/api-spec ::list-events-for-chapter [_]
  (s/keys :req-un [:bridge.api/chapter-id]))

(defmethod api.base/api ::list-events-for-chapter
  [{:datomic/keys [db]
    :keys [active-person-id chapter-id]}]
  (or (chapter.data/check-chapter-organiser db chapter-id active-person-id)
      (mapv #(event.data/event-for-listing db %)
            (event.data/event-ids-by-chapter db chapter-id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Save new event

(defmethod api.base/api-spec ::save-new-event! [_]
  (s/keys :req-un [:bridge.api/chapter-id
                   :bridge/new-event]))

(defmethod api.base/api ::save-new-event!
  [{:datomic/keys [db conn]
    :keys [active-person-id chapter-id new-event]}]
  (or (chapter.data/check-chapter-organiser db chapter-id active-person-id)
      (let [{:event/keys [slug] :as event-tx}
            (event.data/new-event-tx chapter-id active-person-id new-event)

            ;; If the slug is already in the database, append five random digits.
            {:event/keys [slug] :as event-tx}
            (cond-> event-tx
              (some? (event.data/event-id-by-slug db slug))
              (update :event/slug str "-" (rand-int 100000)))

            {db :db-after} (event.data/save-new-event! conn event-tx)]
        (event.data.edit/event-for-editing db [:event/slug slug]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Event for editing

(defmethod api.base/api-spec ::event-for-editing [_]
  (s/keys :req-un [:bridge.api/event-id]))

(defmethod api.base/api ::event-for-editing
  [{:datomic/keys [db]
    :keys [active-person-id event-id]}]
  (or (event.data/check-event-organiser db event-id active-person-id)
      (event.data.edit/event-for-editing db event-id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Update field value

(defmethod api.base/api-spec ::update-field-value! [_]
  (s/keys :req-un [:bridge/field-update]))

(defmethod api.base/api ::update-field-value!
  [{:datomic/keys [db conn]
    :keys [active-person-id]
    {:field/keys [entity-id attr] :as field-update} :field-update}]
  (or (event.data/check-event-organiser db entity-id active-person-id)
      (let [{:keys [error db-after] :as result}
            (data.edit/update-field-value! conn db
                                           event.data.edit/edit-whitelist
                                           field-update)]
        (if (some? error)
          result
          {:value (datomic/attr db-after entity-id attr)}))))
