(ns bridge.event.schema)

(def schema
  [{:db/ident       :event/id
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/uuid
    :db/unique      :db.unique/value}

   {:db/ident       :event/title
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :event/slug
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :event/status
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword}

   {:db/ident       :event/chapter
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/ref
    :db/doc         "ref to :chapter"}

   {:db/ident       :event/organisers
    :db/cardinality :db.cardinality/many
    :db/valueType   :db.type/ref
    :db/doc         "ref to :person"}

   {:db/ident       :event/start-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/end-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/registration-close-date
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/instant}

   {:db/ident       :event/details-markdown
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :event/notes-markdown
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}])
