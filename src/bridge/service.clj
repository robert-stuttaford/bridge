(ns bridge.service
  (:require [ataraxy.core :as ataraxy]
            [bridge.config :as config]
            [bridge.data.datomic :as data.datomic]
            [bridge.web.access :as web.access]
            [bridge.web.client :as web.client]
            [buddy.auth :as buddy]
            [buddy.auth.backends.session :as buddy.session]
            [buddy.auth.middleware :as buddy.middleware]
            [clojure.tools.logging :as logging]
            [integrant.core :as ig]
            [ring.middleware.keyword-params :as ring.keyword-params]
            [ring.middleware.params :as ring.params]
            [ring.middleware.session :as ring.session]
            [ring.middleware.session.cookie :as ring.cookie]
            [ring.util.response :as response]))

(require 'bridge.web.jetty)

(def auth-backend
  (buddy.session/session-backend
   {:unauthorized-handler
    (fn [{:keys [uri] :as req} _]
      (if (buddy/authenticated? req)
        {:status 403}
        (response/redirect (web.access/login-uri uri) )))}))

(defmethod ig/init-key :service/handler [_ {:keys [cookie conn]}]
  (-> (merge-with merge
                  web.access/routes
                  web.client/routes)
      ataraxy/handler
      (data.datomic/wrap-datomic conn)
      (buddy.middleware/wrap-authorization auth-backend)
      (buddy.middleware/wrap-authentication auth-backend)
      ring.keyword-params/wrap-keyword-params
      ring.params/wrap-params
      (ring.session/wrap-session (update cookie :store ring.cookie/cookie-store))))

(defn -main [& args]
  (logging/info "Starting on port " (get-in (config/system) [:adapter/jetty :port]))
  (ig/init (config/system)))
