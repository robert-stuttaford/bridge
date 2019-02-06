(ns web
  (:require [clojure.pprint :as pprint]
            [ring.adapter.jetty :as jetty]))

;; app

(defn page [request]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (with-out-str (pprint/pprint (into (sorted-map) request)))})

(defn middleware-1 [handler]
  (fn [request]
    (let [request  (assoc-in request [:app :m1-req] :m1)
          response (handler request)]
      (assoc-in response [:headers "m1-resp"] "m1"))))

(defn middleware-2 [handler]
  (fn [request]
    (-> request
        (assoc-in [:app :m2-resq] :m2)
        handler
        (assoc-in [:headers "m2-resp"] "m2"))))

(def handler
  (-> page
      middleware-1
      middleware-2))

;; service

(defonce *server (atom nil))

(defn start-server! []
  (reset! *server
          (jetty/run-jetty #'handler
                           {:port  4000
                            :join? false})))

(defn stop-server! []
  (when-some [server @*server]
    (.stop server))
  (reset! *server nil))

(defn reset-server! []
  ;; 1. stop everything
  (stop-server!)
  ;; 2. "stuart sierra reloaded method" reload changed code
  ;; 3. start everything
  (start-server!))

#_(reset-server!)
