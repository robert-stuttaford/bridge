(ns bridge.web.template
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.util.response :as response]
            [rum.core :as rum]))

(def *html-template-source
  (delay (slurp (io/resource (str "template.html")))))

(defn html-template [content]
  (str/replace @*html-template-source "#content#" content))

(defn template-response [content]
  (-> content
      html-template
      response/response))

(defn hiccup-response [& content]
  (-> content
      rum/render-static-markup
      template-response))
