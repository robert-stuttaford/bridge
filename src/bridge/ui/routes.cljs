(ns bridge.ui.routes
  (:require [bidi.bidi :as bidi]
            [bridge.ui.util :refer [<== ==> log]]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def *routes (atom nil))

(defn url->route [url]
  (or (bidi/match-route @*routes url)
      (throw (ex-info (str "No route found for url") {:url url}))))

(defn route->url [{:keys [view params] :as route}]
  (or (apply bidi/path-for @*routes view (mapcat identity params))
      (throw (ex-info (str "Route not defined") route))))

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
   {:href     (route->url {:view   route
                           :params route-params})
    :on-click #(fn [e]
                 (.preventDefault e)
                 (dispatch-route {:handler      route
                                  :route-params route-params}))}))
