(ns bridge.event.ui
  (:require [bridge.ui.ajax :as ui.ajax]
            [bridge.ui.base :as ui.base]
            [bridge.ui.spec :as ui.spec]
            [bridge.ui.util :refer [<== ==>]]
            bridge.event.ui.create
            bridge.event.ui.edit
            bridge.event.ui.list
            [re-frame.core :as rf]))

(rf/reg-sub ::events (fn [db _] (::events db)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; create-event

(rf/reg-event-fx ::save-new-event!
  [ui.spec/check-spec-interceptor]
  (fn [db [_ chapter-id new-event]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/save-new-event!
                     {:chapter-id chapter-id
                      :new-event  new-event}
                     [::save-new-event-complete])}))

(rf/reg-event-db ::save-new-event-complete
  [ui.spec/check-spec-interceptor]
  (fn [db [_ {:event/keys [slug] :as event}]]
    (assoc-in db [::events slug] event)))

(defmethod ui.base/view :create-event [_]
  [bridge.event.ui.create/create-event])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; list-events

(rf/reg-event-fx ::list-events-for-chapter
  [ui.spec/check-spec-interceptor]
  (fn [db [_ chapter-id]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/list-events-for-chapter
                     {:chapter-id chapter-id}
                     [::list-events-for-chapter-complete])}))

(rf/reg-event-db ::list-events-for-chapter-complete
  [ui.spec/check-spec-interceptor]
  (fn [db [_ events]]
    (update db ::events merge
            (into {}
                  (map (juxt :event/slug identity))
                  events))))

(defmethod ui.base/load-on-view :list-events [_]
  [::list-events-for-chapter (<== [:bridge.ui/active-chapter])])

(defmethod ui.base/view :list-events [_]
  [bridge.event.ui.list/list-events])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-event

(rf/reg-event-fx ::event-for-editing
  [ui.spec/check-spec-interceptor]
  (fn [db [_ event-id]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/event-for-editing
                     {:event-id event-id}
                     [::event-for-editing-complete])}))

(rf/reg-event-db ::event-for-editing-complete
  [ui.spec/check-spec-interceptor]
  (fn [db [_ {:event/keys [slug] :as event}]]
    (update db ::events assoc slug event)))

(defmethod ui.base/load-on-view :edit-event [{{:keys [event-slug]} :params}]
  [::event-for-editing [:event/slug event-slug]])

(defmethod ui.base/view :edit-event [{{:keys [event-slug]} :params}]
  [bridge.event.ui.edit/edit-event event-slug])
