(ns bridge.person.data
  (:require [bridge.data.datomic :as datomic]
            bridge.spec
            [buddy.core.codecs :as buddy.codecs]
            [buddy.core.nonce :as buddy.nonce]
            [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

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
    :db/valueType   :db.type/keyword
    :db/doc
    ":status/active    - in good standing
     :status/suspended - by moderator
     :status/deleted   - by person, via self-service"}

   {:db/ident       :person/email-confirm-token
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/value}])

(defn clean-email [email]
  (some-> email
          str/trim
          str/lower-case))

(defn nonce []
  (-> (buddy.nonce/random-nonce 32)
      buddy.codecs/bytes->hex))

(defn new-person-tx [person]
  (-> (s/assert :bridge/new-person person)
      (update :person/email clean-email)
      (update :person/password hashers/derive)
      (assoc :db/id "tempid.person")
      (merge #:person{:status :status/active
                      :email-confirm-token (nonce)})))

(defn person-id-by-email [db email]
  (datomic/entid db [:person/email email]))

(defn password-for-active-person-by-email [db email]
  (datomic/q '[:find ?password .
               :in $ ?email
               :where
               [?person :person/email ?email]
               [?person :person/status :status/active]
               [?person :person/password ?password]]
             db (clean-email email)))

(defn correct-password? [person-password check-password]
  (hashers/check check-password person-password))

(s/def :person/name :bridge.spec/required-string)
(s/def :person/email :bridge.spec/email)
(s/def :person/password :bridge.spec/required-string)

(s/def :bridge/new-person
  (s/keys :req [:person/name
                :person/email
                :person/password]))

(s/def :person/status
  #{:status/active :status/suspended :status/deleted})

(s/def :person/email-confirm-token
  :bridge.spec/nonce)

(s/def :bridge/new-person-tx
  (s/merge :bridge/new-person
           (s/keys :req [:person/status
                         :person/email-confirm-token])))

(s/fdef new-person-tx
        :args (s/cat :new-person :bridge/new-person)
        :ret :bridge/new-person-tx
        :fn #(not= (get-in % [:args :new-person :person/password])
                   (get-in % [:ret :person/password])))
