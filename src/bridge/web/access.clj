(ns bridge.web.access
  (:require [bridge.data.datomic :as datomic]
            [bridge.person.data :as person.data]
            [bridge.web.template :as web.template]
            [ring.util.response :as response]))

(defn login-uri [next]
  (str "/login?next=" next))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Create Profile

(defn create-profile-page [& [error params]]
  (web.template/hiccup-response
   [:section.hero.is-success.is-fullheight
    [:div.hero-body
     [:div.container.has-text-centered
      [:div.column.is-4.is-offset-4
       [:div.box
        [:figure.avatar
         [:img {:src "http://www.clojurebridge.org/assets/logo-small-608079136860146e2095ff960b78fd0d.png"}]]
        (when (some? error)
          [:div.notification.is-warning
           (condp = error
             :account-exists
             (list "Sorry! That email is already registered. Please "
                   [:a {:href (login-uri "/")} "login"] ".")
             :password-too-short
             "Sorry! That password is too short, it needs to be at least 8 characters.")])
        [:form {:method "post"}
         [:div.field
          [:div.control
           [:input.input.is-large
            {:type        "text"
             :name        "name"
             :placeholder "Your Full Name"
             :required    "required"
             :value       (:name params)
             :autofocus   ""}]
           [:p "We use this when organising events."]]]
         [:div.field
          [:div.control
           [:input.input.is-large
            {:type        "email"
             :name        "email"
             :required    "required"
             :value       (:email params)
             :placeholder "Your Email"}]
           [:p "We use this to communicate with you."]]]
         [:div.field
          [:div.control
           [:input.input.is-large
            {:type        "password"
             :name        "password"
             :required    "required"
             :placeholder "Choose a Password"}]]]
         [:button.button.is-block.is-info.is-large.is-fullwidth "Sign Up"]]]
       [:p.has-text-grey
        [:a {:href "/login"} "Login"]
        " · "
        [:a {:href "../"} "Need Help?"]]]]]]))

(defn clean-create-profile-params [params]
  (-> params
      (select-keys [:name :email :password])
      (update :email person.data/clean-email)))

(defn process-create-profile [{:keys [request-method params]
                               :datomic/keys [conn db]}]
  (when (= :post request-method)
    (let [{:keys [name email password]} (clean-create-profile-params params)]
      (cond (some? (person.data/person-id-by-email db email))
            (create-profile-page :account-exists)

            (< (count password) 8)
            (create-profile-page :password-too-short params)

            :else
            (try
              (datomic/transact! conn
                                 [(person.data/new-person-tx
                                   #:person{:name     name
                                            :email    email
                                            :password email})])
              (response/redirect (login-uri "/"))

              (catch Throwable e
                (create-profile-page :unknown-error)))))))

(defn create-profile [request]
  (or (process-create-profile request)
      (create-profile-page)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Login

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
             :required    "required"
             :autofocus   ""}]]]
         [:div.field
          [:div.control
           [:input.input.is-large
            {:type        "password"
             :name        "password"
             :placeholder "Your Password"
             :required    "required"}]]]
         [:button.button.is-block.is-info.is-large.is-fullwidth "Login"]]]
       [:p.has-text-grey
        [:a {:href "/sign-up"} "Sign Up"]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Logout

(defn logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))

(def routes
  {:routes   {"/sign-up" [:sign-up]
              "/login"   [:login]
              "/logout"  [:logout]}
   :handlers {:sign-up #'create-profile
              :login   #'login
              :logout  #'logout}})
