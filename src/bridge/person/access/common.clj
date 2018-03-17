(ns bridge.person.access.common
  (:require [bridge.web.template :as web.template]))

(defn login-uri [next]
  (str "/login?next=" next))

(defn access-page-layout [& content]
  (web.template/hiccup-response
   [:section.hero.is-success.is-fullheight
    [:div.hero-body
     [:div.container.has-text-centered
      [:div.column.is-4.is-offset-4 content]]]]))

(def logo
  [:figure.avatar
   [:img {:src (str "http://www.clojurebridge.org/assets/"
                    "logo-small-608079136860146e2095ff960b78fd0d.png")}]])
