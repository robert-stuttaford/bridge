(ns bridge.ui.routes
  (:require [bidi.bidi :as bidi]
            [bridge.ui.util :refer [<== ==> log]]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def *routes (atom []))

(defn url->route [url]
  (bidi/match-route @*routes url))

(defn route->url [{:keys [view params]}]
  (apply bidi/path-for @*routes view (mapcat identity params)))

(defn dispatch-route [{:keys [handler route-params]}]
  (==> [:bridge.ui/set-view {:view   handler
                             :params route-params}]))

(def history
  (pushy/pushy dispatch-route url->route))

(defn start-routing! [routes]
  (reset! *routes routes)
  (pushy/start! history))

(rf/reg-fx :set-history-token
  (fn [view]
    (pushy/set-token! history (route->url view))))

(defn turbolink
  ([route] (turbolink route {}))
  ([route route-params]
   (let [route {:route        route
                :route-params route-params}]
     {:href     (route->url route)
      :on-click #(fn [e]
                   (.preventDefault e)
                   (dispatch-route route))})))
