(ns bridge.event.api
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.data.datomic :as datomic]
            [bridge.data.edit :as data.edit]
            [bridge.event.data :as event.data]
            [bridge.event.data.edit :as event.data.edit]
            [bridge.web.api.base :as api.base]))

;; TODO spec these args

(defmethod api.base/api ::save-new-event!
  [{:datomic/keys [db conn]
    :keys [active-person-id chapter-id new-event]}]
  (or (chapter.data/check-chapter-organiser db chapter-id active-person-id)
      (let [{:event/keys [slug] :as event-tx}
            (event.data/new-event-tx chapter-id active-person-id new-event)

            {db :db-after} (event.data/save-new-event! conn event-tx)]
        (event.data.edit/event-for-editing db [:event/slug slug]))))

(defmethod api.base/api ::event-details
  [{:datomic/keys [db]
    :keys [active-person-id event-id]}]
  (or (event.data/check-event-organiser db event-id active-person-id)
      (event.data.edit/event-for-editing db event-id)))

(defmethod api.base/api ::update-field-value!
  [{:datomic/keys [db conn]
    :keys [active-person-id event-id field-update]}]
  (or (event.data/check-event-organiser db event-id active-person-id)
      (let [event-id (datomic/entid db event-id) ;; protect against slug changes
            field-update (assoc field-update :field/entity-id event-id)
            {db :db-after} (data.edit/update-field-value! conn db
                                                          event.data.edit/edit-whitelist
                                                          field-update)]
        (event.data.edit/event-for-editing db event-id))))
