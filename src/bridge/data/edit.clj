(ns bridge.data.edit
  (:require [bridge.data.datomic :as datomic]
            [clojure.spec.alpha :as s]))

(defmulti check-custom-validation
  (fn [db {:field/keys [attr]}]
    attr))

(s/fdef check-custom-validation
        :args (s/cat :db :bridge.datomic/db
                     :edit-field :bridge/edit-field-operation)
        :ret (s/or :valid nil?
                   :invalid :bridge/error-result))

(defn check-field-update
  "Returns `{:error :bridge.error/*}` or `nil` if validation succeeds."
  [db attr-whitelist {:field/keys [attr value] :as field}]
  (cond (not (contains? attr-whitelist attr))
        {:error :bridge.error/invalid-edit-key
         attr   value}

        (and (s/spec? attr) (not (s/valid? attr value)))
        {:error      :bridge.error/invalid-edit-value
         attr        value
         :spec-error (s/explain-data attr value)}

        (and (some? (datomic/attr db attr :db/unique))
             (some? (datomic/entid db [attr value])))
        {:error :bridge.error/uniqueness-conflict
         attr   value}

        (and (= (datomic/attr db attr :db/valueType) :db.type/ref)
             (nil? (datomic/entid db value)))
        {:error :bridge.error/missing-referent
         attr   value}

        :else (check-custom-validation db field)))

(s/fdef check-field-update
        :args (s/cat :db :bridge.datomic/db
                     :edit-field :bridge/edit-field-operation)
        :ret (s/or :valid nil?
                   :invalid :bridge/error-result))
