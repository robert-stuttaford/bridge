(ns bridge.web.client
  (:require [bridge.web.template :as web.template]
            [buddy.auth :as buddy]
            [clojure.string :as str]
            [ring.util.response :as response]))

(defn client [req]
  (if-not (buddy/authenticated? req)
    (buddy/throw-unauthorized)
    (web.template/hiccup-response
     [:div#mount]
     [:script {:type "application/edn"}
      (pr-str (:session req))]
     [:script {:src "/js/app.js"}]
     [:script "bridge.main.refresh();"])))

(def routes
  {:routes
   '{"/"                   [:client]
     "/bridge.css"         [:css]
     ^{:re #"/js/.*"} path [:js-resource path]}
   :handlers
   {:client      #'client
    :js-resource #(-> (:uri %)
                      (str/replace #"^/" "")
                      response/resource-response
                      (response/content-type "application/javascript; charset=utf-8"))
    :css         (fn [_] (-> (response/resource-response "bridge.css")
                            (response/content-type "text/css; charset=utf-8")))}})

