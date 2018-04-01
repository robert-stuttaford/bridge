(ns bridge.main
  (:require [ajax.edn]
            [bidi.bidi :as bidi]
            [cljs.reader :as edn]
            [day8.re-frame.http-fx]
            [pushy.core :as pushy]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(def routes
  ["/app/"
   {"" :home
    "events/"
    {"create" :create-event}}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [{:keys [handler route-params]}]
  (rf/dispatch [:set-view handler route-params]))

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))

;;;

(rf/reg-event-fx
  ::http-post
  (fn [db [_ data]]
    {:http-xhrio {:method          :post
                  :uri             "/client/api"
                  :params          data
                  :timeout         5000
                  :format          (ajax.edn/edn-request-format)
                  :response-format (ajax.edn/edn-response-format)
                  :on-success      [::good-http-result]
                  :on-failure      [::bad-http-result]}}))

(rf/reg-event-db
  ::good-http-result
  (fn [db [_ result]]
    (assoc db :good-api-result result)))

(rf/reg-event-db
  ::bad-http-result
  (fn [db [_ result]]
    (assoc db :bad-api-result result)))

(comment
  (rf/dispatch [::http-post
                {:action     :bridge.event.api/save-new-event!
                 :chapter-id [:chapter/slug "clojurebridge-hermanus"]
                 :new-event  #:event{:title      "April Event"
                                     :start-date #inst "2018-04-06"
                                     :end-date   #inst "2018-04-07"}}])
  )

;;;

(defn read-edn-from-script-tag [element]
  (some-> element
          .-textContent
          edn/read-string))

(rf/reg-event-db :initialize
  (fn [_ _]
    (assoc (some-> (js/document.getElementById "initial-data")
                   read-edn-from-script-tag)
           :current-view {:view   :home
                          :params {}})))

(rf/reg-event-db :set-view
  (fn [db [_ view params]]
    (assoc db :current-view {:view   view
                             :params params})))

(rf/reg-sub :active-person (fn [db _] (:active-person db)))
(rf/reg-sub :current-view  (fn [db _] (:current-view db)))

(defn navbar []
  [:nav.navbar.is-fixed-top.is-info
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item {:href "/app/"}
      [:img {:src (str "http://www.clojurebridge.org/assets/"
                       "cb-logo-cbba8a3667c88b78189d8826867cf01a.png")}]]]
    [:div.navbar-menu
     [:div.navbar-start
      [:a.navbar-item {:href "/app/"} "Browse Events"]
      [:a.navbar-item {:href "/app/events/create"} "Create Event"]]
     [:div.navbar-end
      [:div.navbar-item (:person/name @(rf/subscribe [:active-person]))]
      [:a.navbar-item {:href "/logout"} "Sign Out"]]]]])

(defmulti view :view)
(defmethod view :home [_] [:div "home"])
(defmethod view :create-event [_]
  [:div "create-event"
   [:hr]
   [:a.button {:href "javascript:"
               :on-click #(rf/dispatch
                           [::http-post
                            {:action     :bridge.event.api/save-new-event!
                             :chapter-id [:chapter/slug "clojurebridge-hermanus"]
                             :new-event  #:event{:title      "April Event 2"
                                                 :start-date #inst "2018-04-06"
                                                 :end-date   #inst "2018-04-07"}}])}
    "make event"]])

(defn app []
  [:div
   [navbar]
   [:div.container {:style {:margin-top "50px"}}
    (view @(rf/subscribe [:current-view]))]])

(defn ^:export refresh []
  (rf/dispatch-sync [:initialize])
  (app-routes)
  (reagent/render [app] (js/document.getElementById "mount")))
