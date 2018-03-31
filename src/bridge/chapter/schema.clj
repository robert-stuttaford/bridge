(ns bridge.chapter.schema)

(def schema
  [{:db/ident       :chapter/title
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :chapter/slug
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :chapter/status
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword}

   {:db/ident       :chapter/organisers
    :db/cardinality :db.cardinality/many
    :db/valueType   :db.type/ref
    :db/doc         "ref to :person"}

   {:db/ident       :chapter/location
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}])
