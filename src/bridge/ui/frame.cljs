(ns bridge.ui.frame
  (:require [bridge.ui.base :as ui.base]
            [bridge.ui.routes :as ui.routes]
            [bridge.ui.util :refer [<== ==> log]]))

(defmethod ui.base/view :home [_]
  [:div "home"])

(defn navbar []
  [:nav.navbar.is-fixed-top.is-info
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item (ui.routes/turbo-links "/app")
      [:img {:src (str "http://www.clojurebridge.org/assets/"
                       "cb-logo-cbba8a3667c88b78189d8826867cf01a.png")}]]]
    [:div.navbar-menu
     [:div.navbar-start
      [:a.navbar-item (ui.routes/turbo-links "/app/events") "All Events"]
      [:a.navbar-item (ui.routes/turbo-links "/app/events/create") "Create Event"]]
     [:div.navbar-end
      [:div.navbar-item (:person/name (<== [:bridge.ui/active-person]))]
      [:a.navbar-item {:href "/logout"} "Sign Out"]]]]])

(defn app []
  [:div
   [navbar]
   [:div.container {:style {:margin-top "6rem"}}
    (ui.base/view (<== [:bridge.ui/current-view]))]])
