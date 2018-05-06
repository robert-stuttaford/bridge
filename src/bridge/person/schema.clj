(ns bridge.person.schema)

(def schema
  [#:db{:ident       :person/name
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/email
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/value}

   #:db{:ident       :person/password
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/status
        :cardinality :db.cardinality/one
        :valueType   :db.type/keyword}

   #:db{:ident       :person/confirm-email-token
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/value}

   #:db{:ident       :person/reset-password-token
        :cardinality :db.cardinality/one
        :valueType   :db.type/string
        :unique      :db.unique/value}

   #:db{:ident       :person/minor?
        :cardinality :db.cardinality/one
        :valueType   :db.type/boolean}

   #:db{:ident       :person/spoken-languages
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/gender-identity
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/agree-to-code-of-conduct?
        :cardinality :db.cardinality/one
        :valueType   :db.type/boolean}

   #:db{:ident       :person/food-preferences
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/t-shirt-size
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/past-programming-experience
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/experience-with-language?
        :cardinality :db.cardinality/one
        :valueType   :db.type/boolean}

   #:db{:ident       :person/attended-event-before?
        :cardinality :db.cardinality/one
        :valueType   :db.type/boolean}

   #:db{:ident       :person/phone-number
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/coaching-languages
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/past-coaching-experience
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/background-experience
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/preferred-coachee-level
        :cardinality :db.cardinality/one
        :valueType   :db.type/string}

   #:db{:ident       :person/floating-coach?
        :cardinality :db.cardinality/one
        :valueType   :db.type/boolean}])
