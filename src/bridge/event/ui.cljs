(ns bridge.event.ui
  (:require bridge.event.ui.create
            bridge.event.ui.edit
            bridge.event.ui.list
            [re-frame.core :as rf]))

(rf/reg-sub ::events (fn [db _] (::events db)))

;; save-new-event!

(rf/reg-event-fx ::save-new-event!
  (fn [db [_ chapter-id new-event]]
    {:dispatch
     [:bridge.ui.ajax/http-post
      {:action     :bridge.event.api/save-new-event!
       :chapter-id chapter-id
       :new-event  new-event}
      [::save-new-event-complete]]}))

(rf/reg-event-db ::save-new-event-complete
  (fn [db [_ {:event/keys [slug] :as event}]]
    (assoc-in db [::events slug] event)))

;; list-events-for-chapter

(rf/reg-event-fx ::list-events-for-chapter
  (fn [db [_ chapter-id]]
    {:dispatch
     [:bridge.ui.ajax/http-post
      {:action     :bridge.event.api/list-events-for-chapter
       :chapter-id chapter-id}
      [::list-events-for-chapter-complete]]}))

(rf/reg-event-db ::list-events-for-chapter-complete
  (fn [db [_ events]]
    (update db ::events merge
            (into {}
                  (map (juxt :event/slug identity))
                  events))))

;; event-for-editing

(rf/reg-event-fx ::event-for-editing
  (fn [db [_ event-id]]
    {:dispatch
     [:bridge.ui.ajax/http-post
      {:action   :bridge.event.api/event-for-editing
       :event-id event-id}
      [::event-for-editing-complete]]}))

(rf/reg-event-db ::event-for-editing-complete
  (fn [db [_ {:event/keys [slug] :as event}]]
    (update db ::events assoc slug event)))
