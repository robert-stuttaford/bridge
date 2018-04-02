(ns bridge.ui.routes
  (:require [bidi.bidi :as bidi]
            [bridge.ui.util :refer [<== ==>]]
            [pushy.core :as pushy]))

(def *routes (atom []))

(defn url->route [url]
  (bidi/match-route @*routes url))

(defn route->url [route]
  (bidi/path-for @*routes route))

(defn dispatch-route [{:keys [handler route-params]}]
  (==> [:bridge.ui/set-view {:view   handler
                             :params route-params}]))

(defn dispatch-fn-for-route [url]
  (let [route (url->route url)]
    #(fn [e]
       (.preventDefault e)
       (dispatch-route route))))

(defn turbo-links [url]
  {:href     url
   :on-click (dispatch-fn-for-route url)})

(defn start-routing! [routes]
  (reset! *routes routes)
  (pushy/start! (pushy/pushy dispatch-route url->route)))
