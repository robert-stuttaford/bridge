(ns bridge.ui.frame
  (:require [bridge.ui.base :as ui.base]
            [bridge.ui.routes :as ui.routes]
            [bridge.ui.util :refer [<== ==> log]]
            [reagent.core :as r]))

(defmethod ui.base/view :home [_]
  [:div "home"])

(defn add-toggle-menu-handler [{:keys [on-click] :as params} toggle-menu-fn]
  (assoc params :on-click (fn []
                            (on-click)
                            (toggle-menu-fn))))

(defn navbar []
  (let [*menu-active?  (r/atom false)
        toggle-menu-fn #(swap! *menu-active? not)]
    (fn []
      (let [menu-active? @*menu-active?]
        [:nav.navbar.is-fixed-top.is-info
         [:div.container
          [:div.navbar-brand
           [:a.navbar-item (ui.routes/turbolink :home)
            [:img {:src (str "http://www.clojurebridge.org/assets/"
                             "cb-logo-cbba8a3667c88b78189d8826867cf01a.png")}]]
           [:a.navbar-burger (cond-> {:role          "button"
                                      :aria-label    "menu"
                                      :aria-expanded menu-active?
                                      :on-click      toggle-menu-fn}
                               menu-active? (assoc :class "is-active"))
            [:span {:aria-hidden "true"}]
            [:span {:aria-hidden "true"}]
            [:span {:aria-hidden "true"}]]]
          [:div.navbar-menu (when menu-active? {:class "is-active"})
           [:div.navbar-start
            [:a.navbar-item (cond-> (ui.routes/turbolink :list-events)
                              menu-active?
                              (add-toggle-menu-handler toggle-menu-fn)) "All Events"]
            [:a.navbar-item (cond-> (ui.routes/turbolink :create-event)
                              menu-active?
                              (add-toggle-menu-handler toggle-menu-fn)) "Create Event"]]
           [:div.navbar-end
            [:div.navbar-divider]
            [:div.navbar-item (:person/name (<== [:bridge.ui/active-person]))]
            [:a.navbar-item {:href "/logout"} "Sign Out"]]]]]))))

(defn app []
  [:div
   [navbar]
   [:div.container {:style {:margin-top "6rem"}}
    (ui.base/view (<== [:bridge.ui/current-view]))]])
