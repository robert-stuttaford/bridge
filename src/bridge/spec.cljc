(ns bridge.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(def not-blank? (complement str/blank?))

(s/def :bridge.spec/required-string
  (s/and string? not-blank?))

(s/def :bridge.spec/optional-string
  (s/or :empty str/blank? :str not-blank?))

(def email-regex
  "http://emailregex.com/"
  #?(:cljs
     #"^(([^<>()\[\]\\.,;:\s@\"]+(\.[^<>()\[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$"
     :clj
     #"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"))

(s/def :bridge.spec/email
  #(re-matches email-regex %))

(def nonce-regex
  #"[0-9a-f]{64}")

(s/def :bridge.spec/nonce
  #(re-matches nonce-regex %))

(def slug-regex
  #"[0-9a-z-]+")

(s/def :bridge.spec/slug
  #(re-matches slug-regex %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Errors

(s/def :bridge/error
  (s/and keyword?
         #(let [ns (namespace %)]
            (and (re-find #"^bridge" ns)
                 (re-find #"error$" ns)))))

(s/def :bridge/error-result
  (s/keys :req-un [:bridge/error]))
