(ns bridge.data.string
  #?@
  (:clj
   [(:require [clojure.string :as str])]
   :cljs
   [(:require [clojure.string :as str]
              cljsjs.showdown)
    (:import goog.i18n.DateTimeFormat)]))

(defn not-blank [s]
  (when-not (str/blank? s)
    s))

(def keyword->label
  (comp str/capitalize
        #(str/replace % "[-_]" " ")
        name))

#?(:cljs
   (let [converter (js/showdown.Converter.)]
     (defn markdown->html [markdown-txt]
       (.makeHtml converter (or markdown-txt "")))))
