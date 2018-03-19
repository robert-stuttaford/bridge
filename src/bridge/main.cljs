(ns bridge.main
  (:require [cljs.reader :as edn]
            [rum.core :as rum]))

(enable-console-print!)

(defn read-edn-from-script-tag [element]
  (some-> element
          .-textContent
          edn/read-string))

(rum/defc navbar [state]
  [:nav.navbar.is-fixed-top.is-info
   [:.container
    [:.navbar-brand
     [:a.navbar-item {:href "/"}
      [:img {:src (str "http://www.clojurebridge.org/assets/"
                       "cb-logo-cbba8a3667c88b78189d8826867cf01a.png")}]]]
    [:.navbar-menu
     [:.navbar-start
      [:a.navbar-item {:href "javascript:"} "Browse Events"]
      [:a.navbar-item {:href "javascript:"} "Add New Event"]]
     [:.navbar-end
      [:.navbar-item (get-in state [:person :person/name])]
      [:a.navbar-item {:href "/logout"} "Sign Out"]]]]])

(rum/defc app [initial-data]
  [:div
   (navbar initial-data)
   [:.container {:style {:margin-top "50px"}}]])

(defn ^:export refresh []
  (rum/mount (app (some-> (js/document.getElementById "initial-data")
                          read-edn-from-script-tag))
             (js/document.getElementById "mount")))
