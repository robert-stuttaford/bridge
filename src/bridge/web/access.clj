(ns bridge.web.access
  (:require [bridge.web.template :as web.template]
            [buddy.hashers :as hashers]
            [ring.util.response :as response]))

(def authdata
  "Global var that stores valid users with their
   respective passwords."
  {:admin (hashers/derive "secret")
   :test  (hashers/derive "secret")})

(defn login [{:keys [request-method params]}]
  (or (when (= :post request-method)
        (let [username (:username params)
              password (:password params)]
          (when (hashers/check password (get authdata (keyword username)))
            (-> (response/redirect (:next params "/"))
                (assoc-in [:session :identity] (keyword username))))))
      (web.template/hiccup-response
       [:h1 "Login"]
       [:form {:method "post"}
        [:input {:type        "text"
                 :placeholder "Username:"
                 :value       "test"
                 :name        "username"}]
        [:input {:type        "password"
                 :placeholder "Password:"
                 :value       "secret"
                 :name        "password"}]
        [:input {:type  "submit"
                 :value "Submit"}]])))

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(def routes
  {:routes   {"/login"  [:login]
              "/logout" [:logout]}
   :handlers {:login  login
              :logout logout}})
