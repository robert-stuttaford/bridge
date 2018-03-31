(ns bridge.web.api
  (:require [bridge.web.api.base :as api.base]))

;; TODO use `transit` in production

(defn handle-api [request]
  (let [{:keys [action] :as payload} (api.base/request->api-payload request)]
    (if action
      {:body (or (api.base/api payload)
                 {:result :ok})}
      {:status 400
       :body   "No :action provided"})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Routes

(def routes
  {:routes
   '{[:post "/client/api"] ^:authenticated? [:client-api]}
   :handlers
   {:client-api  #'handle-api}})
