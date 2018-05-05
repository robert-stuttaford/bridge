(ns bridge.person.ui
  (:require [bridge.person.spec :as person.spec]
            bridge.person.ui.edit-profile
            [bridge.ui.ajax :as ui.ajax]
            [bridge.ui.base :as ui.base]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.spec :as ui.spec]
            [re-frame.core :as rf]))

(def routes
  {"/edit-profile" :edit-profile})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; db

(rf/reg-sub ::profile-for-editing (fn [db _] (::profile-for-editing db)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-profile

(defmethod ui.base/load-on-view :edit-profile [_]
  [::edit-profile])

(defmethod ui.base/view :edit-profile [_]
  [bridge.person.ui.edit-profile/edit-profile])

(rf/reg-event-fx ::edit-profile
  [ui.spec/check-spec-interceptor]
  (fn [db _]
    {:dispatch
     (ui.ajax/action :bridge.person.api/profile-for-editing {}
                     [::edit-profile-complete])}))

(rf/reg-event-db ::edit-profile-complete
  [ui.spec/check-spec-interceptor]
  (fn [db [_ profile]]
    (assoc db ::profile-for-editing
           (ui.edit-field/prepare-edit-attrs profile person.spec/attr->field-config))))

(rf/reg-event-fx ::update-field-value!
  [ui.spec/check-spec-interceptor]
  (fn [_ [_ {:field/keys [attr] :as field} edit-value]]
    {:dispatch
     (ui.ajax/action :bridge.person.api/update-field-value!
                     {:field-update
                      #:field{:attr attr
                              :value edit-value}}
                     [::update-field-value-complete field])}))

(rf/reg-event-fx ::update-field-value-complete
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ {:field/keys [attr edit-by-default?]} {:keys [error value]}]]
    (if (some? error)
      {:db (update-in db [::profile-for-editing attr]
                      #(-> %
                           (dissoc :busy?)
                           (assoc :error error)))}
      {:db (assoc-in db [::profile-for-editing attr]
                     (ui.edit-field/init-edit-state edit-by-default? value))})))
