(ns bridge.person.access.login
  (:require [bridge.data.datomic :as datomic]
            [bridge.person.access.common :as access.common]
            [bridge.person.data :as person.data]
            [ring.util.response :as response]))

(defn login-page [& [error]]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    (when (some? error)
      [:div.notification.is-warning
       (condp = error
         :incorrect-details
         "Sorry! Those details aren't working."

         :confirm-email-first
         "Sorry! You need to confirm your email first.")])
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
    [:a {:href "/forgot-password"} "Forgot Password"]
    " · "
    [:a {:href "/help"} "Need Help?"]]))

(defn login [{:keys [request-method]
              {:keys [email password next] :or {next "/"}} :params
              :datomic/keys [db]}]
  (or (when (= :post request-method)
        (let [email     (person.data/clean-email email)
              person-id (person.data/person-id-by-email db email)]
          (cond (nil? person-id)
                (login-page :incorrect-details)

                (some? (datomic/attr db person-id :person/confirm-email-token))
                (login-page :confirm-email-first)

                (person.data/correct-password?
                 (datomic/attr db person-id :person/password)
                 password)
                (-> (response/redirect next)
                    (assoc-in [:session :identity] [:person/email email])))))
      (login-page)))
