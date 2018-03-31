(ns bridge.person.data-test
  (:require [bridge.data.datomic :as datomic]
            [bridge.person.data :as person.data]
            [bridge.person.schema :as person.schema]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is use-fixtures]])
  (:import clojure.lang.ExceptionInfo))

(def db-name (str *ns*))

(use-fixtures :once test-setup)
(use-fixtures :each (with-database db-name person.schema/schema))

(def TEST-EMAIL "test@cb.org")
(def TEST-PASSWORD "secret")
(defn TEST-NEW-PERSON-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (person.data/new-person-tx #:person{:name     "Test Person"
                                      :email    TEST-EMAIL
                                      :password TEST-PASSWORD}))

(deftest nonce
  (is (s/valid? :bridge.spec/nonce (person.data/nonce))))

(deftest check-password-validity
  (is (nil? (person.data/check-password-validity "abcdefgh" "abcdefgh")))

  (is (= :password-too-short
         (person.data/check-password-validity "" "")))

  (is (= :passwords-do-not-match
         (person.data/check-password-validity "abcdefgh"
                                      "ABCDEFGH"))))

(deftest hash-and-check-password
  (let [hashed-password (person.data/hash-password TEST-PASSWORD)]
    (is (person.data/correct-password? hashed-password TEST-PASSWORD))
    (is (not (person.data/correct-password? hashed-password "wrong password")))))

(deftest new-person-tx
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (person.data/new-person-tx {})))

  (is (s/valid? :bridge/new-person-tx (TEST-NEW-PERSON-TX)))
  (is (= :status/active (:person/status (TEST-NEW-PERSON-TX)))))

(deftest save-person!

  (let [new-person-tx (TEST-NEW-PERSON-TX)]

    (person.data/save-new-person! (conn db-name) new-person-tx)

    (let [new-db (db db-name)]
      (is (person.data/person-id-by-email new-db TEST-EMAIL))
      (is (person.data/person-id-by-confirm-email-token
           new-db (:person/confirm-email-token new-person-tx))))))

(deftest confirm-email!

  (let [new-person-tx (TEST-NEW-PERSON-TX)]
    (person.data/save-new-person! (conn db-name) new-person-tx)

    (person.data/confirm-email! (conn db-name)
                                (person.data/person-id-by-email (db db-name) TEST-EMAIL)
                                (:person/confirm-email-token new-person-tx))

    (let [new-db (db db-name)]
      (is (nil? (person.data/person-id-by-confirm-email-token
                 new-db (:person/confirm-email-token new-person-tx))))

      (is (nil? (datomic/attr new-db (person.data/person-id-by-email new-db TEST-EMAIL)
                              :person/confirm-email-token))))))

(def mock-nonce (constantly (person.data/nonce)))

(deftest request-reset-password!
  (person.data/save-new-person! (conn db-name) (TEST-NEW-PERSON-TX))

  (binding [person.data/nonce mock-nonce]
    (person.data/request-password-reset!
     (conn db-name)
     (person.data/person-id-by-email (db db-name) TEST-EMAIL))

    (let [new-db    (db db-name)
          token (mock-nonce)]
      (is (person.data/person-id-by-reset-password-token
           new-db token))

      (is (= (datomic/attr new-db (person.data/person-id-by-email new-db TEST-EMAIL)
                           :person/reset-password-token)
             token)))))

(deftest reset-password!
  (person.data/save-new-person! (conn db-name) (TEST-NEW-PERSON-TX))

  (binding [person.data/nonce mock-nonce]
    (person.data/request-password-reset!
     (conn db-name)
     (person.data/person-id-by-email (db db-name) TEST-EMAIL))

    (let [new-db    (db db-name)
          token (mock-nonce)]

      (is (person.data/correct-password?
           (datomic/attr new-db (person.data/person-id-by-email new-db TEST-EMAIL)
                         :person/password)
           TEST-PASSWORD))

      (person.data/reset-password!
       (conn db-name)
       (person.data/person-id-by-reset-password-token new-db token)
       token
       "new-secret")

      (let [new-db (db db-name)
            new-password
            (datomic/attr new-db (person.data/person-id-by-email new-db TEST-EMAIL)
                          :person/password)]
        (is (nil? (datomic/attr new-db (person.data/person-id-by-email new-db TEST-EMAIL)
                                :person/reset-password-token)))

        (is (not (person.data/correct-password? new-password TEST-PASSWORD)))

        (is (person.data/correct-password? new-password "new-secret"))))))
