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

  (let [tx (person.data/new-person-tx {:person/email    "test@cg.org"
                                       :person/password "secret"})]

    (is (s/valid? :bridge/new-person-tx tx))
    (is (= :status/active (:person/status tx)))

    (let [db       (db-after-tx [tx])
          person   (d/entity db [:person/email "test@cg.org"])
          password (person.data/password-for-active-person-by-email db "test@cg.org")]

      (is (some? person))
      (is (person.data/correct-password? password "secret"))
      (is (not (person.data/correct-password? password "wrong")))

      )))
