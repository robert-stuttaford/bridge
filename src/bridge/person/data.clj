(ns bridge.person.data
  (:require [bridge.data.datomic :as datomic]
            [buddy.core.codecs :as buddy.codecs]
            [buddy.core.nonce :as buddy.nonce]
            [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.logging :as logging]))

(require 'bridge.person.spec)

(defn clean-email [email]
  (some-> email str/trim str/lower-case))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Crypto

(defn ^:dynamic nonce []
  (-> (buddy.nonce/random-nonce 32)
      buddy.codecs/bytes->hex))

(def hash-password hashers/derive)

(defn check-password-validity [password confirm-password]
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
  (logging/debug :send-email! email body))

(defn new-person-tx [person]
  (-> (s/assert :bridge/new-person person)
      (assoc :db/id "tempid.person")
      (update :person/email clean-email)
      (update :person/password hash-password)
      (merge #:person{:status :status/active
                      :confirm-email-token (nonce)})))

(s/fdef new-person-tx
        :args (s/cat :new-person :bridge/new-person)
        :ret :bridge/new-person-tx
        :fn #(not= (get-in % [:args :new-person :person/password])
                   (get-in % [:ret :person/password])))

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