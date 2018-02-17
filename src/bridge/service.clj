(ns bridge.service
  (:require [ataraxy.core :as ataraxy]
            [clojure.string :as str]
            [integrant.core :as ig]
            [ring.util.response :as response]))

(defn app-page [req]
  {:status  200
   :headers {}
   :body    "<div id=\"mount\"></div><script src=\"/js/app.js\"></script>"})

(defn js-resource [req]
  (-> (:uri req)
      (str/replace #"^/" "")
      response/resource-response))

(def routes
  '{"/" [:app-page]
    ^{:re #"/js/.*"} path [:js-resource path]})

(def handler
  (ataraxy/handler
   {:routes   routes
    :handlers {:app-page    app-page
               :js-resource js-resource}}))

(defmethod ig/init-key :service/handler [_ _]
  handler)
