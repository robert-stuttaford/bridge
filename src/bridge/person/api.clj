(ns bridge.person.api
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.edit :as data.edit]
            [bridge.person.data.edit :as person.data.edit]
            [bridge.web.api.base :as api.base]
            [clojure.spec.alpha :as s]))

(require 'bridge.data.datomic.spec
         'bridge.data.edit.spec)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Profile for editing

(defmethod api.base/api ::profile-for-editing
  [{:datomic/keys [db]
    :keys [active-person-id]}]
  (person.data.edit/profile-for-editing db active-person-id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Update field value

(defmethod api.base/api-spec ::update-field-value! [_]
  (s/keys :req-un [:bridge/field-update]))

(defmethod api.base/api ::update-field-value!
  [{:datomic/keys [db conn]
    :keys [active-person-id]
    {:field/keys [attr] :as field-update} :field-update}]
  (let [{:keys [error db-after] :as result}
        (data.edit/update-field-value! conn db
                                       person.data.edit/edit-whitelist
                                       (assoc field-update :field/entity-id
                                              active-person-id))]
    (if (some? error)
      result
      {:value (datomic/attr db-after active-person-id attr)})))
