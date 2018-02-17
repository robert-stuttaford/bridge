(ns bridge.web.jetty
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]))

(defmethod ig/init-key :adapter/jetty [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (-> opts (dissoc :handler) (assoc :join? false))]
    {:handler handler
     :server  (jetty/run-jetty (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :adapter/jetty [_ {:keys [server]}]
  (.stop server))

(defmethod ig/suspend-key! :adapter/jetty [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :adapter/jetty [key opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))
