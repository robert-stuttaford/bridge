(ns bridge.web.api
  (:require [bridge.web.api.base :as api.base]
            [clojure.spec.alpha :as s]))

;; TODO use `transit` in production

(defn check-spec-error [payload]
  (let [spec (api.base/api-spec payload)]
    (when (and (some? spec) (not (s/valid? spec payload)))
      {:status 400
       :body   (s/explain-str spec payload)})))

(defn handle-api [request]
  (let [{:keys [action] :as payload} (api.base/request->api-payload request)]
    (if action
      (or (check-spec-error payload)
          {:body (pr-str (or (api.base/api payload)
                             {:result :ok}))})
      {:status 400
       :body   "No :action provided"})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Routes

(def routes
  {:routes
   '{[:post "/client/api"] ^:authenticated? [:client-api]}
   :handlers
   {:client-api  #'handle-api}})
