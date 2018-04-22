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

(defn dispatch-fn-for-route [url]
  (let [route (url->route url)]
    #(fn [e]
       (.preventDefault e)
       (dispatch-route route))))

(def history (pushy/pushy dispatch-route url->route))

(rf/reg-fx :set-history-token
  (fn [view]
    (pushy/set-token! history (route->url view))))

(defn start-routing! [routes]
  (reset! *routes routes)
  (pushy/start! history))

(defn turbo-links [url]
  {:href     url
   :on-click (dispatch-fn-for-route url)})
