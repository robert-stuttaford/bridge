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

(s/def :bridge/field-update
  (s/keys :req [:field/attr :field/value]
          :opt [:field/entity-id :field/retract?]))

(s/def :field/type #{:text :multiline-text :email :markdown :checkbox})
(s/def :field/edit-state-key keyword?)
(s/def :field/placeholder :bridge.spec/optional-string)
(s/def :field/title :bridge.spec/required-string)

(s/def :bridge/edit-field-config
  (s/keys :req [:field/type :field/edit-state-key :field/attr :field/title]
          :opt [:field/placeholder]))
