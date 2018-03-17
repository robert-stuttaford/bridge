(ns bridge.person.data
  (:require [buddy.hashers :as hashers]
            [clojure.string :as str]
            [datomic.api :as d]))

(def schema
  [{:db/ident       :person/email
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}

   {:db/ident       :person/password
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}

   {:db/ident       :person/status
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword
    :db/doc
    ":status/active    - in good standing
     :status/suspended - by moderator
     :status/deleted   - by person, via self-service"}])

(defn clean-email [email]
  (some-> email
          str/trim
          str/lower-case))

(defn new-person-tx [person]
  (-> person
      (update :person/email clean-email)
      (update :person/password hashers/derive)
      (merge {:db/id         "tempid.person"
              :person/status :status/active})))

(defn password-for-active-person-by-email [db email]
  (d/q '[:find ?password .
         :in $ ?email
         :where
         [?person :person/email ?email]
         [?person :person/status :status/active]
         [?person :person/password ?password]]
       db (clean-email email)))

(defn correct-password? [person-password check-password]
  (hashers/check check-password person-password))
