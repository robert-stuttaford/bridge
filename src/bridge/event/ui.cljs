(ns bridge.event.ui
  (:require bridge.event.ui.create
            bridge.event.ui.edit
            bridge.event.ui.list
            [bridge.ui.ajax :as ui.ajax]
            [bridge.ui.base :as ui.base]
            [bridge.ui.spec :as ui.spec]
            [bridge.ui.util :refer [<== ==>]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [re-frame.core :as rf]))

(def routes
  {"/events" {""                     :list-events
              "/create"              :create-event
              ["/edit/" :event-slug] :edit-event}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; events db

(rf/reg-sub ::events (fn [db _] (::events db)))

(defn set-event [db {:event/keys [slug] :as event}]
  (update db ::events assoc
          [:event/slug slug] event))

(defn set-events [db events]
  (update db ::events merge
          (into {}
                (map (juxt (fn [{:event/keys [slug]}]
                             [:event/slug slug])
                           identity))
                events)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; create-event

(rf/reg-event-fx ::save-new-event!
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ chapter-id new-event]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/save-new-event!
                     {:chapter-id chapter-id
                      :new-event  new-event}
                     [::save-new-event-complete])}))

(rf/reg-event-db ::save-new-event-complete
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ event]]
    (set-event db event)))

(defmethod ui.base/view :create-event [_]
  [bridge.event.ui.create/create-event])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; list-events

(rf/reg-event-fx ::list-events-for-chapter
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ chapter-id]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/list-events-for-chapter
                     {:chapter-id chapter-id}
                     [::list-events-for-chapter-complete])}))

(rf/reg-event-db ::list-events-for-chapter-complete
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ events]]
    (set-events db events)))

(defmethod ui.base/load-on-view :list-events [_]
  [::list-events-for-chapter (<== [:bridge.ui/active-chapter])])

(defmethod ui.base/view :list-events [_]
  [bridge.event.ui.list/list-events])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-event

(rf/reg-event-fx ::event-for-editing
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ event-id]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/event-for-editing
                     {:event-id event-id}
                     [::event-for-editing-complete])}))

(rf/reg-event-db ::event-for-editing-complete
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ event]]
    (set-event db event)))

(rf/reg-event-fx ::update-field-value!
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ field-update]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/update-field-value!
                     {:field-update field-update}
                     [::update-field-value-complete field-update])}))

(rf/reg-event-db ::update-field-value-complete
  [ui.spec/check-spec-interceptor]
  (fn-traced [db [_ field-update event]]
    (set-event db event)
    ;; TODO if (:field-update/attr field-update) is :event/slug :
    ;; - dissoc old slug key?
    ;; - go to edit url for new slug
    ))

(defmethod ui.base/load-on-view :edit-event [{{:keys [event-slug]} :params}]
  [::event-for-editing [:event/slug event-slug]])

(defmethod ui.base/view :edit-event [{{:keys [event-slug]} :params}]
  [bridge.event.ui.edit/edit-event event-slug])
