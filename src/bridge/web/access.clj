(ns bridge.web.access
  (:require [bridge.web.template :as web.template]
            [buddy.hashers :as hashers]
            [ring.util.response :as response]))

(def auth-data
  {"admin@cb.org" (hashers/derive "secret")
   "test@cb.org"  (hashers/derive "secret")})

(defn login [{:keys [request-method]
              {:keys [email password next] :or {next "/"}} :params}]
  (or (when (and (= :post request-method)
                 (hashers/check password (get auth-data email)))
        (-> (response/redirect next)
            (assoc-in [:session :identity] email)))
      (web.template/hiccup-response
       [:section.hero.is-success.is-fullheight
        [:div.hero-body
         [:div.container.has-text-centered
          [:div.column.is-4.is-offset-4
           [:h3.title.has-text-grey "Login"]
           [:p.subtitle.has-text-grey "Please login to proceed."]
           [:div.box
            [:figure.avatar
             [:img {:src "http://www.clojurebridge.org/assets/logo-small-608079136860146e2095ff960b78fd0d.png"}]]
            [:form {:method "post"}
             [:div.field
              [:div.control
               [:input.input.is-large
                {:type        "email"
                 :name        "email"
                 :placeholder "Your Email"
                 :autofocus   ""}]]]
             [:div.field
              [:div.control
               [:input.input.is-large
                {:type        "password"
                 :name        "password"
                 :placeholder "Your Password"}]]]
             [:div.field
              [:label.checkbox [:input {:type "checkbox"}]
               " Remember me"]]
             [:button.button.is-block.is-info.is-large.is-fullwidth "Login"]]]
           [:p.has-text-grey
            [:a {:href "../"} "Sign Up"]
            "  ·  "
            [:a {:href "../"} "Forgot Password"]
            "  ·  "
            [:a {:href "../"} "Need Help?"]]]]]])))

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(def routes
  {:routes   {"/login"  [:login]
              "/logout" [:logout]}
   :handlers {:login  login
              :logout logout}})
