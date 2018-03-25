(ns bridge.event.api
  (:require [bridge.event.data :as event.data]
            [bridge.web.api.base :as api.base]
            [bridge.chapter.data :as chapter.data]))

(defmethod api.base/api ::edit-event [{:datomic/keys [db]
                                       :keys [active-user-id event-slug]}]
  (let [event-id (event.data/event-id-by-slug db event-slug)]
    (or (event.data/check-event-organiser db event-id active-user-id)
        (->> (event.data/event-id-by-slug db event-slug)
             (event.data/event-for-editing db)))))

(defmethod api.base/api ::save-new-event! [{:datomic/keys [db conn]
                                            :keys [active-user-id chapter-id new-event]
                                            :as orig-payload}]
  (or (chapter.data/check-chapter-organiser db chapter-id active-user-id)
      (let [{:event/keys [slug] :as event-tx}
            (event.data/new-event-tx chapter-id active-user-id new-event)

            {db :db-after} (event.data/save-new-event! conn event-tx)]
        (api.base/api-with-new-payload orig-payload
                                       {:datomic/db db
                                        :action     ::edit-event
                                        :event-slug slug}))))
