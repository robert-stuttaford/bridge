(ns bridge.person.access
  (:require [bridge.person.access.common :as access.common]
            [bridge.person.access.confirm-email :as access.confirm-email]
            [bridge.person.access.create-profile :as access.create-profile]
            [bridge.person.access.forgot-password :as access.forgot-password]
            [bridge.person.access.login :as access.login]
            [bridge.person.access.reset-password :as access.reset-password]
            [ring.util.response :as response]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Help

(defn help [request]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    [:p "[how to get in touch with a human]"]]
   [:p.has-text-grey
    [:a {:href "/login"} "Login"]
    " · "
    [:a {:href "/sign-up"} "Sign Up"]
    " · "
    [:a {:href "/forgot-password"} "Forgot Password"]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Logout

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(def routes
  {:routes   '{"/sign-up"                 [:sign-up]
               ["/confirm-email/" token]  [:confirm-email token]
               "/forgot-password"         [:forgot-password]
               ["/reset-password/" token] [:reset-password token]
               "/login"                   [:login]
               "/logout"                  [:logout]
               "/help"                  [:help]}
   :handlers {:sign-up         #'access.create-profile/create-profile
              :confirm-email   #'access.confirm-email/confirm-email
              :forgot-password #'access.forgot-password/forgot-password
              :reset-password  #'access.reset-password/reset-password
              :login           #'access.login/login
              :logout          #'logout
              :help            #'help}})
