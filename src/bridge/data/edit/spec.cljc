(ns bridge.data.edit.spec
  (:require [clojure.spec.alpha :as s]))

(require 'bridge.spec
         'bridge.data.datomic.spec)

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

(s/def :field/type #{:text :email :markdown})
(s/def :field/subscription (s/tuple keyword?))
(s/def :field/placeholder :bridge.spec/optional-string)
(s/def :field/title :bridge.spec/required-string)
(s/def :field/commit-action keyword?)

(s/def :bridge/edit-field-config
  (s/keys :req [:field/type :field/subscription :field/entity-id :field/attr
                :field/title :field/commit-action]
          :opt [:field/placeholder]))
