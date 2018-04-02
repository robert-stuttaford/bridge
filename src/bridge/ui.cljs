(ns bridge.ui
  (:require [bridge.ui.base :as ui.base]
            [bridge.ui.routes :as ui.routes]
            [re-frame.core :as rf]))

(rf/reg-sub ::active-person (fn [db _] (::active-person db)))
(rf/reg-sub ::active-chapter (fn [db _] (::active-chapter db)))

(defn navbar []
  [:nav.navbar.is-fixed-top.is-info
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item (ui.routes/turbo-links "/app/events")
      [:img {:src (str "http://www.clojurebridge.org/assets/"
                       "cb-logo-cbba8a3667c88b78189d8826867cf01a.png")}]]]
    [:div.navbar-menu
     [:div.navbar-start
      [:a.navbar-item (ui.routes/turbo-links "/app/events") "All Events"]
      [:a.navbar-item (ui.routes/turbo-links "/app/events/create") "Create Event"]]
     [:div.navbar-end
      [:div.navbar-item (:person/name @(rf/subscribe [::active-person]))]
      [:a.navbar-item {:href "/logout"} "Sign Out"]]]]])

(defmethod ui.base/view :home [_] [:div "home"])

;;; View handling

(rf/reg-event-fx ::set-view
  (fn [{:keys [db]} [_ view]]
    (let [load-on-view (ui.base/load-on-view view)]
      (cond-> {:db (assoc db ::current-view view)}
        (some? load-on-view) (assoc :dispatch load-on-view)))))

(def initial-state
  {::current-view {:view   :home
                   :params {}}})

(rf/reg-sub ::current-view (fn [db _] (::current-view db)))

(defn app []
  [:div
   [navbar]
   [:div.container {:style {:margin-top "50px"}}
    (ui.base/view @(rf/subscribe [::current-view]))]])
