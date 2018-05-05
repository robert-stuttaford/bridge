(ns bridge.person.api-test
  (:require [bridge.person.api :as person.api]
            [bridge.person.data :as person.data]
            [bridge.person.schema :as person.schema]
            [bridge.test.fixtures :as fixtures :refer [TEST-PERSON-ID]]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [bridge.web.api.base :as api.base]
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

(defn TEST-PAYLOAD []
  {:datomic/db       (db db-name)
   :datomic/conn     (conn db-name)
   :active-person-id TEST-PERSON-ID})

(deftest profile-for-editing

  (person.data/save-new-person! (conn db-name) (TEST-NEW-PERSON-TX))

  (let [result (api.base/api (merge (TEST-PAYLOAD)
                                    {:action ::person.api/profile-for-editing}))]

    (is (= (get-in result [:person/name])
           "Test Person"))))

(deftest update-field-value!

  (person.data/save-new-person! (conn db-name) (TEST-NEW-PERSON-TX))

  (let [result (api.base/api (merge (TEST-PAYLOAD)
                                    {:action       ::person.api/update-field-value!
                                     :field-update
                                     #:field{:attr      :person/food-preferences
                                             :value     "Pizza"}}))]

    (is (= (:value result)
           "Pizza"))))
