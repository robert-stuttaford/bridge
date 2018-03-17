(ns bridge.person.access.confirm-email
  (:require [bridge.person.access.common :as access.common]
            [bridge.person.data :as person.data]))

(defn confirm-email-page [error]
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    [:div.notification.is-warning
     (case error
       :invalid-token "Sorry! That token is invalid."
       :unknown-error "Sorry! Something went wrong.")]]
   [:p.has-text-grey
    [:a {:href "/help"} "Need Help?"]]))

(defn confirm-email-success-page []
  (access.common/access-page-layout
   [:div.box
    access.common/logo
    [:div.notification.is-info
     [:p "Great! Your email has been validated."]
     [:p "Please " [:a {:href (access.common/login-uri "/")} "log in"] "."]]]))

(defn confirm-email [{[_ token] :ataraxy/result
                      :datomic/keys [conn db]}]
  (or (when-some [person-id (person.data/person-id-by-confirm-email-token db token)]
        (try
          (person.data/confirm-email! conn person-id token)

          (confirm-email-success-page)

          (catch Throwable e
            (confirm-email-page :unknown-error))))
      (confirm-email-page :invalid-token)))
