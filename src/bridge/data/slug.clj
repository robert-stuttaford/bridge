(ns bridge.data.slug
  (:require [clojure.string :as str])
  (:import [java.text Normalizer Normalizer$Form]))

;; adapted from https://github.com/rlander/slugify

(defn- normalize [str]
  (-> (Normalizer/normalize str Normalizer$Form/NFD)
      (str/replace #"[\P{ASCII}]+" "")
      str/lower-case))

(defn ->slug
  "Slugs are limited to 250 characters."
  [str]
  (let [slug (-> (normalize str)
                 str/triml
                 (str/split #"[\p{Space}\p{P}]+")
                 (->> (str/join "-")))]
    (subs slug 0
          (min 250 (count slug)))))
