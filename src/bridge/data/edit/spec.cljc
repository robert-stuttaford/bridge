(ns bridge.data.edit.spec
  (:require [clojure.spec.alpha :as s]))

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
