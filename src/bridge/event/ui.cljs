(ns bridge.event.ui
  (:require [bridge.event.spec :as event.spec]
            bridge.event.ui.create
            bridge.event.ui.edit
            bridge.event.ui.list
            [bridge.ui.ajax :as ui.ajax]
            [bridge.ui.base :as ui.base]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.spec :as ui.spec]
            [bridge.ui.util :refer [<== ==> log]]
            [re-frame.core :as rf]))

(def routes
  {"/events" {""                     :list-events
              "/create"              :create-event
              ["/edit/" :event-slug] :edit-event}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; events db

(rf/reg-sub ::events (fn [db _] (::events db)))

(defn set-event [db {:event/keys [id] :as event}]
  (update db ::events assoc [:event/id id] event))

(defn set-events [db events]
  (update db ::events merge
          (into {}
                (map (juxt (fn [{:event/keys [id]}]
                             [:event/id id])
                           identity))
                events)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; create-event

(defmethod ui.base/view :create-event [_]
  [bridge.event.ui.create/create-event])

(rf/reg-event-fx ::save-new-event!
  [ui.spec/check-spec-interceptor]
  (fn [db [_ chapter-id new-event]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/save-new-event!
                     {:chapter-id chapter-id
                      :new-event  new-event}
                     [::save-new-event-complete])}))

(rf/reg-event-fx ::save-new-event-complete
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ {:event/keys [slug] :as event}]]
    {:db                (set-event db event)
     :set-history-token {:view   :edit-event
                         :params {:event-slug slug}}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; list-events

(defmethod ui.base/load-on-view :list-events [_]
  [::list-events-for-chapter (<== [:bridge.ui/active-chapter])])

(defmethod ui.base/view :list-events [_]
  [bridge.event.ui.list/list-events])

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
    (set-events db events)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-event

(defmethod ui.base/load-on-view :edit-event [{{:keys [event-slug]} :params}]
  [::event-for-editing [:event/slug event-slug]])

(defmethod ui.base/view :edit-event [{{:keys [event-slug]} :params}]
  [bridge.event.ui.edit/edit-event event-slug])

(rf/reg-sub ::event-for-editing (fn [db _] (::event-for-editing db)))

(rf/reg-event-fx ::event-for-editing
  [ui.spec/check-spec-interceptor]
  (fn [db [_ event-id]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/event-for-editing
                     {:event-id event-id}
                     [::event-for-editing-complete])}))

(rf/reg-event-db ::event-for-editing-complete
  [ui.spec/check-spec-interceptor]
  (fn [db [_ event]]
    (assoc db ::event-for-editing
           (ui.edit-field/prepare-edit-attrs event event.spec/attr->field-config))))

(rf/reg-event-fx ::update-field-value!
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ {:field/keys [attr] :as field} edit-value]]
    {:dispatch
     (ui.ajax/action :bridge.event.api/update-field-value!
                     {:field-update
                      (let [event-id (get-in db [::event-for-editing :event/id])]
                        #:field{:entity-id [:event/id event-id]
                                :attr      attr
                                :value     edit-value})}
                     [::update-field-value-complete field])}))

(rf/reg-event-fx ::update-field-value-complete
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ {:field/keys [attr edit-by-default?]} {:keys [error value]}]]
    (if (some? error)
      {:db (update-in db [::event-for-editing attr]
                      #(-> %
                           (dissoc :busy?)
                           (assoc :error error)))}
      (cond-> {:db (assoc-in db [::event-for-editing attr]
                             (ui.edit-field/init-edit-state edit-by-default? value))}
        (= attr :event/slug)
        (assoc :set-history-token {:view   :edit-event
                                   :params {:event-slug value}})))))
