(ns bridge.web.template
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [hiccup.core :as hiccup]
            [ring.util.response :as response]))

(def *html-template-source
  (delay (slurp (io/resource (str "template.html")))))

(defn html-template [content]
  (str/replace @*html-template-source "#content#" content))

(defn template-response [content]
  (-> content
      html-template
      response/response))

(defn hiccup-response [& content]
  (template-response (hiccup/html content)))
