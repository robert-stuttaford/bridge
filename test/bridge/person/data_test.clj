(ns bridge.person.data-test
  (:require [bridge.person.data :as person.data]
            [bridge.test.util :refer [db test-setup with-database]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is use-fixtures]]
            [datomic.api :as d])
  (:import clojure.lang.ExceptionInfo))

(def db-name (str *ns*))

(use-fixtures :once test-setup)
(use-fixtures :each (with-database db-name person.data/schema))

(defn db-after-tx [tx]
  (:db-after (d/with (db db-name) tx)))

(deftest new-person-tx
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (person.data/new-person-tx {})))

  (let [TEST-EMAIL    "test@cb.org"
        TEST-PASSWORD "secret"
        tx            (person.data/new-person-tx #:person{:name     "Test Person"
                                                          :email    TEST-EMAIL
                                                          :password TEST-PASSWORD})]

    (is (s/valid? :bridge/new-person-tx tx))
    (is (= :status/active (:person/status tx)))

    (let [db       (db-after-tx [tx])
          person   (d/entity db [:person/email TEST-EMAIL])
          password (person.data/password-for-active-person-by-email db TEST-EMAIL)]

      (is (some? person))
      (is (person.data/correct-password? password TEST-PASSWORD))
      (is (not (person.data/correct-password? password "wrong")))

      )))
