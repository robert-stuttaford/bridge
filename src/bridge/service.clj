(ns bridge.service
  (:require [ataraxy.core :as ataraxy]
            [buddy.auth :as buddy]
            [buddy.auth.backends.session :as buddy.session]
            [buddy.auth.middleware :as buddy.middleware]
            [clojure.string :as str]
            [integrant.core :as ig]
            [ring.middleware.params :as ring.params]
            [ring.middleware.session :as ring.session]
            [ring.util.response :as response]))

(defn home [req]
  (if-not (buddy/authenticated? req)
    (buddy/throw-unauthorized)
    (-> (response/response "<div id=\"mount\"></div><script src=\"/js/app.js\"></script>")
        (response/content-type "text/html; charset=utf-8"))))

(defn login [req]
  (-> (response/response "<h1>Login</h1>
<form method=\"post\">
    <input type=\"text\" placeholder=\"Username:\" value=\"test\" name=\"username\" />
    <input type=\"password\" placeholder=\"Password:\" value=\"secret\" name=\"password\" />
    <input type=\"submit\" value=\"Submit\" />
</form>")
      (response/content-type "text/html; charset=utf-8")))

(def authdata
  "Global var that stores valid users with their
   respective passwords."
  {:admin "secret"
   :test  "secret"})

(defn process-login [{:keys [session form-params query-params] :as req}]
  (let [username (get form-params "username")
        password (get form-params "password")]
    (if (= (get authdata (keyword username)) password)
      (-> (response/redirect (:next query-params "/"))
          (assoc :session (assoc session :identity (keyword username))))
      (login req))))

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(defn js-resource [req]
  (-> (:uri req)
      (str/replace #"^/" "")
      response/resource-response))

(def routes
  '{"/"                   [:home]
    "/login"              {[:get]  [:login]
                           [:post] [:process-login]}
    "/logout"             [:logout]
    ^{:re #"/js/.*"} path [:js-resource path]})

(def handler
  (ataraxy/handler
   {:routes   routes
    :handlers {:home          home
               :js-resource   js-resource
               :login         login
               :process-login process-login
               :logout        logout}}))

(def auth-backend
  (buddy.session/session-backend
   {:unauthorized-handler
    (fn [{:keys [uri] :as req} _]
      (if (buddy/authenticated? req)
        {:status 403}
        (response/redirect (str "/login?next=" uri))))}))

(defmethod ig/init-key :service/handler [_ _]
  (-> handler
      (buddy.middleware/wrap-authorization auth-backend)
      (buddy.middleware/wrap-authentication auth-backend)
      ring.params/wrap-params
      ring.session/wrap-session))
