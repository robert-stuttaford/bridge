(ns bridge.person.access.reset-password
  (:require [bridge.person.access.common :as access.common]
            [bridge.person.data :as person.data]
            [ring.util.response :as response]))

(defn reset-password-page [& [error]]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    (when (some? error)
      [:div.notification.is-warning
       (condp = error
         :invalid-token
         "Sorry! That token is invalid."

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
        {:type        "password"
         :name        "password"
         :placeholder "Your Password"
         :required    "required"}]]]
     [:div.field
      [:div.control
       [:input.input.is-large
        {:type        "password"
         :name        "confirm-password"
         :placeholder "Confirm Password"
         :required    "required"}]]]
     [:button.button.is-block.is-info.is-large.is-fullwidth "Reset Password"]]]
   [:p.has-text-grey
    [:a {:href "/help"} "Need Help?"]]))

(defn reset-password [{[_ token] :ataraxy/result
                       :keys [request-method]
                       {:keys [password confirm-password]} :params
                       :datomic/keys [conn db]}]
  (or (when (= :post request-method)
        (let [person-id (person.data/person-id-by-reset-password-token
                         db token)
              password-error (person.data/check-password-validity password confirm-password)]
          (cond (nil? person-id)
                (reset-password-page :invalid-token)

                (some? password-error)
                (reset-password-page password-error)

                :else
                (try
                  (person.data/reset-password! conn person-id token password)

                  (response/redirect (access.common/login-uri "/"))

                  (catch Throwable e
                    (reset-password-page :unknown-error))))))
      (reset-password-page)))
