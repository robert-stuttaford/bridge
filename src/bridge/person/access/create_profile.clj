(ns bridge.person.access.create-profile
  (:require [bridge.person.access.common :as access.common]
            [bridge.person.data :as person.data]))

(defn create-profile-page [& [error params]]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    (when (some? error)
      [:div.notification.is-warning
       (condp = error
         :account-exists
         (list "Sorry! That email is already registered. Please "
               [:a {:href (access.common/login-uri "/")} "log in"] ".")

         :password-too-short
         "Sorry! That password is too short, it needs to be at least 8 characters."

         :passwords-do-not-match
         "Sorry! Those passwords do not match."

         :unknown-error
         "Sorry! Something went wrong.")])
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
     [:div.field
      [:div.control
       [:input.input.is-large
        {:type        "password"
         :name        "confirm-password"
         :placeholder "Confirm Password"
         :required    "required"}]]]
     [:button.button.is-block.is-info.is-large.is-fullwidth "Sign Up"]]]
   [:p.has-text-grey
    [:a {:href "/login"} "Login"]
    " · "
    [:a {:href "/help"} "Need Help?"]]))

(defn create-profile-success-page []
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    [:div.notification.is-info
     [:p "Thanks! We need you to confirm your email address."]
     [:p "Please check your email now."]]]))

(defn create-profile [{:keys [request-method params]
                       :datomic/keys [conn db]}]
  (or (when (= :post request-method)
        (let [params (update params :email person.data/clean-email)

              {:keys [name email password confirm-password]} params

              password-error (person.data/check-password-validity password confirm-password)]
          (cond (some? (person.data/person-id-by-email db email))
                (create-profile-page :account-exists)

                (some? password-error)
                (create-profile-page password-error params)

                :else
                (try
                  (->> (person.data/new-person-tx #:person{:name     name
                                                           :email    email
                                                           :password email})
                       (person.data/save-new-person! conn))

                  (create-profile-success-page)

                  (catch Throwable e
                    (create-profile-page :unknown-error))))))
      (create-profile-page)))
