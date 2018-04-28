(ns bridge.web.client
  (:require [bridge.data.datomic :as datomic]
            [bridge.web.template :as web.template]
            [clojure.string :as str]
            [ring.util.response :as response]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Base page

(defn edn-script-tag [id data]
  [:script
   {:id   id
    :type "application/edn"}
   (pr-str data)])

(defn client [{:datomic/keys [db]
               {:keys [identity]} :session}]
  (web.template/hiccup-response
   [:div#mount]
   (edn-script-tag
    "initial-state"
    #:bridge.ui{:active-person  (datomic/pull db [:person/name] identity)
                ;; TODO parameterise
                :active-chapter [:chapter/slug "clojurebridge-hermanus"]})
   [:script {:src "/js/app.js"}]
   [:script "bridge.main.refresh();"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Routes

(def routes
  {:routes
   '{"/"                   [:redirect-to-client]
     ^{:re #"^/app.*"} app ^:authenticated? [:client]
     "/bridge.css"         [:css]
     ^{:re #"/js/.*"} path [:js-resource path]}
   :handlers
   {:redirect-to-client (fn [_] (response/redirect "/app"))
    :client             #'client
    :js-resource
    #(-> (:uri %)
         (str/replace #"^/" "")
         response/resource-response
         (response/content-type "application/javascript; charset=utf-8"))
    :css
    (fn [_] (-> (response/resource-response "bridge.css")
               (response/content-type "text/css; charset=utf-8")))}})
