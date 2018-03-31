(ns bridge.person.schema)

(def schema
  [{:db/ident       :person/name
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :person/email
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :person/password
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :person/status
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword}

   {:db/ident       :person/confirm-email-token
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :person/reset-password-token
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}])
