(ns bridge.data.edit
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.string :as data.string]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(require 'bridge.data.edit.spec)

(defmulti check-custom-validation
  (fn [db {:field/keys [attr]}]
    attr))

(defmethod check-custom-validation :default [_ _] nil)

(defn check-custom-validation*
  "So that defmethod doesn't find instrumentation output"
  [db field]
  (check-custom-validation db field))

(s/fdef check-custom-validation*
        :args (s/cat :db :bridge.datomic/db
                     :edit-field :bridge/field-update)
        :ret (s/or :valid nil?
                   :invalid :bridge/error-result))

(defn check-field-update:invalid-edit-key [db attr-whitelist {:field/keys [attr]}]
  (when-not (contains? attr-whitelist attr)
    {:error      :bridge.error/invalid-edit-key
     :field/attr attr}))

(defn check-field-update:invalid-edit-value [db {:field/keys [attr value]}]
  (when (and (s/get-spec attr) (not (s/valid? attr value)))
    {:error       :bridge.error/invalid-edit-value
     :field/attr  attr
     :field/value value
     :spec-error  (s/explain-data attr value)}))

(defn check-field-update:uniqueness-conflict [db {:field/keys [attr value]}]
  (when (and (datomic/attr-is-unique? db attr)
             (some? (datomic/entid db [attr value])))
    {:error       :bridge.error/uniqueness-conflict
     :field/attr  attr
     :field/value value}))

(defn check-field-update:missing-referent [db {:field/keys [attr value]}]
  (when (and (datomic/attr-is-ref? db attr)
             (nil? (datomic/entid db value)))
    {:error       :bridge.error/missing-referent
     :field/attr  attr
     :field/value value}))

(defn check-field-update
  "Returns `{:error :bridge.error/*}` or `nil` if validation succeeds."
  [db attr-whitelist field]
  (or (check-field-update:invalid-edit-key    db attr-whitelist field)
      (check-field-update:invalid-edit-value  db field)
      (check-field-update:uniqueness-conflict db field)
      (check-field-update:missing-referent    db field)
      (check-custom-validation*               db field)))

(s/fdef check-field-update
        :args (s/cat :db :bridge.datomic/db
                     :whitelist (s/coll-of keyword? :kind set?)
                     :edit-field :bridge/field-update)
        :ret (s/or :valid nil?
                   :invalid :bridge/error-result))

(defn update-field-value-tx [db {:field/keys [entity-id attr value retract?]}]
  (if (and (= (datomic/attr-type db attr) :db.type/string)
           (str/blank? value))
    (when-some [existing-value (-> (datomic/attr db entity-id attr)
                                   data.string/not-blank)]
      [:db/retract entity-id attr existing-value])
    [(if retract? :db/retract :db/add) entity-id attr value]))

(s/fdef update-field-value-tx
        :args (s/cat :db :bridge.datomic/db
                     :edit-field :bridge/field-update)
        :ret (s/tuple #{:db/add :db/retract}
                      :bridge.datomic/stored-id
                      keyword?
                      :bridge.datomic/scalar-value))

(defn update-field-value! [conn db attr-whitelist field]
  (or (check-field-update db attr-whitelist field)
      (datomic/transact! conn [(update-field-value-tx db field)])))
