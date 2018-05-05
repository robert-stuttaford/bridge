(ns bridge.person.data.edit-test
  (:require [bridge.person.data :as person.data]
            [bridge.person.data.edit :as person.data.edit]
            [bridge.person.schema :as person.schema]
            [bridge.test.fixtures :as fixtures :refer [TEST-PERSON-ID]]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]]))

(def db-name (str *ns*))

(use-fixtures :once test-setup)
(use-fixtures :each (join-fixtures [(with-database db-name person.schema/schema)]))

(def TEST-EMAIL "test@cb.org")
(def TEST-PASSWORD "secret")

(defn TEST-NEW-PERSON-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (person.data/new-person-tx #:person{:name     "Test Person"
                                      :email    TEST-EMAIL
                                      :password TEST-PASSWORD}))

(deftest profile-for-editing

  (person.data/save-new-person! (conn db-name) (TEST-NEW-PERSON-TX))

  (let [new-db (db db-name)
        for-editing (person.data.edit/profile-for-editing new-db TEST-PERSON-ID)]
    (is (= (get-in for-editing [:person/name])
           "Test Person"))))
