(ns bridge.main
  (:require [rum.core :as rum]))

(enable-console-print!)

(rum/defc app []
  [:div "Hello"
   [:br]
   [:a {:href "/logout"} "Logout"]])

(defn ^:export refresh []
  (rum/mount (app) (js/document.getElementById "mount")))
