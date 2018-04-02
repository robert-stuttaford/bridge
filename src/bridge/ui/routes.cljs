(ns bridge.ui.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def routes
  ["/app"
   {""        :home
    "/events"
    {""                     :list-events
     "/create"              :create-event
     ["/edit/" :event-slug] :edit-event}}])

(defn url->route [url]
  (bidi/match-route routes url))

(def route->url (partial bidi/path-for routes))

(defn dispatch-route [{:keys [handler route-params]}]
  (rf/dispatch [:bridge.ui/set-view {:view   handler
                                     :params route-params}]))

(defn dispatch-fn-for-route [url]
  (let [route (url->route url)]
    #(fn [e]
       (.preventDefault e)
       (dispatch-route route)) ))

(defn turbo-links [url]
  {:href     url
   :on-click (dispatch-fn-for-route url)})

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route url->route)))
