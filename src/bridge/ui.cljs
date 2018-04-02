(ns bridge.ui
  (:require [bridge.ui.base :as ui.base]
            [bridge.ui.spec :as ui.spec]
            [re-frame.core :as rf]))

(rf/reg-sub ::active-person (fn [db _] (::active-person db)))
(rf/reg-sub ::active-chapter (fn [db _] (::active-chapter db)))

(def initial-state
  {::current-view {:view   :home
                   :params {}}})

(rf/reg-event-fx ::set-view
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ view]]
    (let [load-on-view (ui.base/load-on-view view)]
      (cond-> {:db (assoc db ::current-view view)}
        (some? load-on-view) (assoc :dispatch load-on-view)))))

(rf/reg-sub ::current-view (fn [db _] (::current-view db)))
