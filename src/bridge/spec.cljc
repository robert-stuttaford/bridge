(ns bridge.spec
  #?@
   (:clj
    [(:require [clojure.spec.alpha :as s] [clojure.string :as str])
     (:import datomic.client.impl.shared.protocols.Db datomic.Database)]
    :cljs
    [(:require [clojure.spec.alpha :as s] [clojure.string :as str])]))

(def not-blank? (complement str/blank?))

(s/def ::required-string
  (s/and string? not-blank?))

(s/def ::optional-string
  (s/or :empty str/blank? :str not-blank?))

(def email-regex
  "http://emailregex.com/"
  #?(:cljs
     #"^(([^<>()\[\]\\.,;:\s@\"]+(\.[^<>()\[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$"
     :clj
     #"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"))

(s/def ::email
  #(re-matches email-regex %))

(def nonce-regex
  #"[0-9a-f]{64}")

(s/def ::nonce
  #(re-matches nonce-regex %))

(def slug-regex
  #"[0-9a-z-]+")

(s/def ::slug
  #(re-matches slug-regex %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Datomic transactions and pull results

;; Only used server-side

#?(:clj
   (s/def :bridge.datomic/db
     (s/or :peer #(instance? datomic.Database %)
           :client #(satisfies? datomic.client.impl.shared.protocols.Db %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Datomic transactions and pull results

(s/def :bridge.datomic/scalar-value
  (s/or :long integer?
        :instant inst?
        :string ::required-string
        :boolean boolean?
        :keyword keyword?))

(s/def :bridge.datomic/value
  (s/or :scalar :bridge.datomic/scalar-value
        :ref :bridge.datomic/ref
        :ref-coll (s/coll-of :bridge.datomic/ref)))

(s/def :bridge.datomic/partitioned-tempid
  (s/keys :req-un [:bridge.datomic.tempid/part :bridge.datomic.tempid/idx]))

(s/def :bridge.datomic.tempid/part #{:db.part/user})
(s/def :bridge.datomic.tempid/idx neg-int?)

(s/def :bridge.datomic/tempid
  (s/or :string ::required-string
        :partition :bridge.datomic/partitioned-tempid))

(s/def :bridge.datomic/lookup-ref
  (s/tuple :bridge.datomic.lookup-ref/attr :bridge.datomic.lookup-ref/val))

(s/def :bridge.datomic.lookup-ref/attr keyword?)
(s/def :bridge.datomic.lookup-ref/val :bridge.datomic/scalar-value)

(s/def :bridge.datomic/stored-id
  (s/or :lookup-ref :bridge.datomic/lookup-ref
        :id pos-int?))

(s/def :bridge.datomic/id
  (s/or :tempid :bridge.datomic/tempid
        :stored-id :bridge.datomic/stored-id))

(s/def :bridge.datomic/ref
  (s/or :id :bridge.datomic/id
        :map (s/map-of keyword? :bridge.datomic/value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Field edit operations

(s/def :field/entity-id :bridge.datomic/stored-id)
(s/def :field/attr keyword?)
(s/def :field/value
  (s/or :scalar :bridge.datomic/scalar-value
        :lookup-ref :bridge.datomic/lookup-ref
        :lookup-ref-coll (s/coll-of :bridge.datomic/lookup-ref)))

(s/def :field/retract? boolean?)

(s/def :bridge/edit-field-operation
  (s/keys :req [:field/entity-id :field/attr :field/value]
          :opt [:field/retract?]))

(s/def :bridge/error
  (s/and keyword?
         #(= (namespace %) "bridge.error")))

(s/def :bridge/error-result
  (s/keys :req-un [:bridge/error]))
