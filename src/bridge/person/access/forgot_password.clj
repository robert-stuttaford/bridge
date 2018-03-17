(ns bridge.person.access.forgot-password
  (:require [bridge.person.access.common :as access.common]
            [bridge.person.data :as person.data]))

(defn forgot-password-page [& [error]]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    (when (some? error)
      [:div.notification.is-warning
       (condp = error
         :unknown-email
         (list "Sorry! That email isn't on our system. Perhaps you should "
               [:a {:href "/sign-up"} "sign up"] " instead?")

         :unknown-error
         "Sorry! Something went wrong.")])
    [:form {:method "post"}
     [:div.field
      [:div.control
       [:input.input.is-large
        {:type        "email"
         :name        "email"
         :placeholder "Your Email"
         :required    "required"}]]]
     [:button.button.is-block.is-info.is-large.is-fullwidth "Request Password Reset"]]]
   [:p.has-text-grey
    [:a {:href "/help"} "Need Help?"]]))

(defn forgot-password-success-page []
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    [:div.notification.is-info
     "Please check your email for instructions now."]]))

(defn forgot-password [{:keys [request-method]
                        {:keys [email]} :params
                        :datomic/keys [conn db]}]
  (or (when (= :post request-method)
        (let [person-id (person.data/person-id-by-email db email)]
          (if (nil? person-id)
            (forgot-password-page :unknown-email)
            (try
              (person.data/request-password-reset! conn person-id)

              (forgot-password-success-page)

              (catch Throwable e
                (forgot-password-page :unknown-error))))))
      (forgot-password-page)))
