(ns bridge.web.access
  (:require [bridge.person.data :as person.data]
            [bridge.web.template :as web.template]
            [ring.util.response :as response]))

(defn login-uri [next]
  (str "/login?next=" next))

(defn login-page [& [incorrect-details]]
  (web.template/hiccup-response
   [:section.hero.is-success.is-fullheight
    [:div.hero-body
     [:div.container.has-text-centered
      [:div.column.is-4.is-offset-4
       [:div.box
        [:figure.avatar
         [:img {:src "http://www.clojurebridge.org/assets/logo-small-608079136860146e2095ff960b78fd0d.png"}]]
        (when (some? incorrect-details)
          [:div.notification.is-warning "Sorry! Those details aren't working."])
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
         [:button.button.is-block.is-info.is-large.is-fullwidth "Login"]]]
       [:p.has-text-grey
        [:a {:href "../"} "Sign Up"]
        " · "
        [:a {:href "../"} "Forgot Password"]
        " · "
        [:a {:href "../"} "Need Help?"]]]]]]))

(defn process-login [{:keys [request-method]
                      {:keys [email password next] :or {next "/"}} :params
                      :datomic/keys [db]}]
  (when (= :post request-method)
    (if (some-> (person.data/password-for-active-person-by-email db email)
                (person.data/correct-password? password))
      (-> (response/redirect next)
          (assoc-in [:session :identity] [:person/email email]))
      (login-page :incorrect-details))))

(defn login [request]
  (or (process-login request)
      (login-page)))

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(def routes
  {:routes   {"/login"  [:login]
              "/logout" [:logout]}
   :handlers {:login  #'login
              :logout #'logout}})
