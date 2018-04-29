(ns bridge.data.datomic.spec
  #?@
   (:clj
    [(:require
      [clojure.spec.alpha :as s]
      [datomic.client.impl.shared.protocols :as datomic.client.protocols])
     (:import datomic.Database)]
    :cljs
    [(:require [clojure.spec.alpha :as s])]))

#?(:clj
   (s/def :bridge.datomic/db
     (s/or :peer #(instance? datomic.Database %)
           :client #(satisfies? datomic.client.protocols/Db %))))

(s/def :bridge.datomic/scalar-value
  (s/or :long integer?
        :instant inst?
        :string string?
        :boolean boolean?
        :keyword keyword?
        :uuid uuid?))

(s/def :bridge.datomic/value
  (s/or :scalar :bridge.datomic/scalar-value
        :ref :bridge.datomic/ref
        :ref-coll (s/coll-of :bridge.datomic/ref)))

(s/def :bridge.datomic/partitioned-tempid
  (s/keys :req-un [:bridge.datomic.tempid/part :bridge.datomic.tempid/idx]))

(s/def :bridge.datomic.tempid/part #{:db.part/user})
(s/def :bridge.datomic.tempid/idx neg-int?)

(s/def :bridge.datomic/tempid
  (s/or :string :bridge.spec/required-string
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
