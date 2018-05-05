(ns bridge.event.schema)

(def schema
  [#:db{:ident       :event/id
        :cardinality :db.cardinality/one
        :valueType   :db.type/uuid
        :unique      :db.unique/value}

   #:db{:ident       :event/title
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :event/slug
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/value}

   #:db{:ident       :event/status
        :cardinality :db.cardinality/one
        :valueType   :db.type/keyword}

   #:db{:ident       :event/chapter
        :cardinality :db.cardinality/one
        :valueType   :db.type/ref
        :doc         "ref to :chapter"}

   #:db{:ident       :event/organisers
        :cardinality :db.cardinality/many
        :valueType   :db.type/ref
        :doc         "ref to :person"}

   #:db{:ident       :event/start-date
        :cardinality :db.cardinality/one
        :valueType   :db.type/instant}

   #:db{:ident       :event/end-date
        :cardinality :db.cardinality/one
        :valueType   :db.type/instant}

   #:db{:ident       :event/registration-close-date
        :cardinality :db.cardinality/one
        :valueType   :db.type/instant}

   #:db{:ident       :event/details-markdown
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :event/notes-markdown
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}])
