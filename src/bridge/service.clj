(ns bridge.service
  (:require [ataraxy.core :as ataraxy]
            [bridge.config :as config]
            [bridge.data.datomic :as data.datomic]
            [bridge.person.access :as person.access]
            [bridge.person.access.common :as access.common]
            [bridge.web.api :as web.api]
            [bridge.web.client :as web.client]
            [bridge.web.debug :as web.debug]
            [buddy.auth :as buddy]
            [buddy.auth.backends.session :as buddy.session]
            [buddy.auth.middleware :as buddy.middleware]
            [clojure.tools.logging :as logging]
            [integrant.core :as ig]
            [ring.middleware.cljsjs :as ring.cljsjs]
            [ring.middleware.keyword-params :as ring.keyword-params]
            [ring.middleware.params :as ring.params]
            [ring.middleware.session :as ring.session]
            [ring.middleware.session.cookie :as ring.cookie]
            [ring.middleware.stacktrace :as ring.stacktrace]
            [ring.util.response :as response]))

(require 'bridge.data.dev-data
         'bridge.event.api
         'bridge.person.api
         'bridge.web.jetty)

(def auth-backend
  (buddy.session/session-backend
   {:unauthorized-handler
    (fn [{:keys [uri] :as req} _]
      (if (buddy/authenticated? req)
        {:status 403}
        (response/redirect (access.common/login-uri uri))))}))

(def common-routing
  {:middleware
   {:authenticated? (fn [handler]
                      (fn [request]
                        (if-not (buddy/authenticated? request)
                          (buddy/throw-unauthorized)
                          (handler request))))}})

(defmethod ig/init-key :service/handler [_ {:keys [cookie datomic]}]
  (-> (merge-with merge
                  common-routing
                  person.access/routes
                  web.api/routes
                  web.client/routes
                  web.debug/routes)
      ataraxy/handler
      (data.datomic/wrap-datomic datomic)
      (buddy.middleware/wrap-authorization auth-backend)
      (buddy.middleware/wrap-authentication auth-backend)
      ring.keyword-params/wrap-keyword-params
      ring.params/wrap-params
      ring.cljsjs/wrap-cljsjs
      (ring.session/wrap-session (update cookie :store ring.cookie/cookie-store))
      (ring.stacktrace/wrap-stacktrace-web)))

(defn -main [& args]
  (logging/info "Starting on port " (get-in (config/system) [:adapter/jetty :port]))
  (ig/init (config/system)))
