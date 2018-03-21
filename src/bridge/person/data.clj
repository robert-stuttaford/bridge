(ns bridge.person.data
  (:require [bridge.data.datomic :as datomic]
            bridge.spec
            [buddy.core.codecs :as buddy.codecs]
            [buddy.core.nonce :as buddy.nonce]
            [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.logging :as logging]))

(defn clean-email [email]
  (some-> email str/trim str/lower-case))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Crypto

(defn ^:dynamic nonce []
  (-> (buddy.nonce/random-nonce 32)
      buddy.codecs/bytes->hex))

(def hash-password hashers/derive)

(defn valid-password? [password confirm-password]
  (cond (< (count password) 8)           :password-too-short
        (not= password confirm-password) :passwords-do-not-match
        :else                            nil))

(defn correct-password? [person-password check-password]
  (hashers/check check-password person-password))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Queries

(defn person-id-by-email [db email]
  (datomic/entid db [:person/email (clean-email email)]))

(defn person-id-by-confirm-email-token [db token]
  (datomic/entid db [:person/confirm-email-token token]))

(defn person-id-by-reset-password-token [db token]
  (datomic/entid db [:person/reset-password-token token]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Transactions

(defn send-email! [email body]
  ;; TODO actually send `body` to `email`
  (logging/info :send-email! email body))

(defn new-person-tx [person]
  (-> (s/assert :bridge/new-person person)
      (assoc :db/id "tempid.person")
      (update :person/email clean-email)
      (update :person/password hash-password)
      (merge #:person{:status :status/active
                      :confirm-email-token (nonce)})))

(defn save-new-person! [conn person-tx]
  (datomic/transact! conn [person-tx])
  (let [{:person/keys [email confirm-email-token]} person-tx]
    (send-email! email (str "your email confirm token: " confirm-email-token))))

(defn confirm-email! [conn person-id token]
  (datomic/transact! conn
                     [[:db/retract person-id :person/confirm-email-token token]]))

(defn request-password-reset! [conn person-id]
  (let [token (nonce)
        {:keys [db-after]} (datomic/transact! conn
                                              [[:db/add person-id
                                                :person/reset-password-token token]])
        email (datomic/attr db-after person-id :person/email)]
    (send-email! email (str "your password reset token: " token))))

(defn reset-password! [conn person-id token new-password]
  (datomic/transact! conn
                     [[:db/retract person-id :person/reset-password-token token]
                      [:db/add person-id :person/password (hash-password new-password)]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Schema

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Specs

(s/def :person/name :bridge.spec/required-string)
(s/def :person/email :bridge.spec/email)
(s/def :person/password :bridge.spec/required-string)
(s/def :person/status #{:status/active :status/suspended :status/deleted})
(s/def :person/confirm-email-token :bridge.spec/nonce)
(s/def :person/reset-password-token :bridge.spec/nonce)

(s/def :bridge/new-person
  (s/keys :req [:person/name :person/email :person/password]))

(s/def :bridge/person
  (s/merge :bridge/new-person (s/keys :req [:person/status])))

(s/def :bridge/new-person-tx
  (s/merge :bridge/person (s/keys :req [:person/confirm-email-token])))

(s/fdef new-person-tx
        :args (s/cat :new-person :bridge/new-person)
        :ret :bridge/new-person-tx
        :fn #(not= (get-in % [:args :new-person :person/password])
                   (get-in % [:ret :person/password])))
