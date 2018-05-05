(ns bridge.ui
  (:require bridge.person.ui
            bridge.event.ui
            [bridge.ui.base :as ui.base]
            [bridge.ui.spec :as ui.spec]
            [cljs.reader :as edn]
            [re-frame.core :as rf]))

(def app-routes
  ["/app"
   (merge {"" :home}
          bridge.person.ui/routes
          bridge.event.ui/routes)])

(rf/reg-sub ::active-person (fn [db _] (::active-person db)))
(rf/reg-sub ::active-chapter (fn [db _] (::active-chapter db)))
(rf/reg-sub ::current-view (fn [db _] (::current-view db)))

(rf/reg-sub ::network-error (fn [db _] (::network-error db)))

(def initial-state
  {::current-view {:view   :home
                   :params {}}})

(rf/reg-event-db ::initialize
  [ui.spec/check-spec-interceptor]
  (fn [_ _]
    (merge (some-> (js/document.getElementById "initial-state")
                   .-textContent
                   edn/read-string)
           initial-state)))

(rf/reg-event-fx ::set-view
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ view]]
    (let [load-on-view (ui.base/load-on-view view)]
      (cond-> {:db (assoc db ::current-view view)}
        (some? load-on-view) (assoc :dispatch load-on-view)))))
