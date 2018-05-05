(ns bridge.chapter.schema)

(def schema
  [#:db{:ident       :chapter/title
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :chapter/slug
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/value}

   #:db{:ident       :chapter/status
        :cardinality :db.cardinality/one
        :valueType   :db.type/keyword}

   #:db{:ident       :chapter/organisers
        :cardinality :db.cardinality/many
        :valueType   :db.type/ref
        :doc         "ref to :person"}

   #:db{:ident       :chapter/location
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}])
