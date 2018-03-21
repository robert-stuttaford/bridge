(ns bridge.event.data-test
  (:require [bridge.data.dev-data :as dev-data]
            [bridge.data.slug :as slug]
            [bridge.event.data :as event.data]
            [bridge.person.data :as person.data]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]]
            [datomic.api :as d])
  (:import clojure.lang.ExceptionInfo))

(def db-name (str *ns*))

(defn person-fixtures [db-name]
  (fn [test-fn]
    (let [conn (conn db-name)]
      @(d/transact conn person.data/schema)

      (dev-data/add-person! conn
                            #:person{:name     "Test Name"
                                     :email    "test@cb.org"
                                     :password "secret"}))

    (test-fn)))

(use-fixtures :once test-setup)
(use-fixtures :each (join-fixtures [(with-database db-name event.data/schema)
                                    (person-fixtures db-name)]))

(def TEST-EVENT-TITLE "April Event")
(def TEST-EVENT-SLUG (slug/->slug TEST-EVENT-TITLE))
(def TEST-PERSON-ID [:person/email "test@cb.org"])

(defn TEST-NEW-EVENT-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (event.data/new-event-tx TEST-PERSON-ID
                           #:event{:title      "April Event"
                                   :start-date #inst "2018-04-06"
                                   :end-date   #inst "2018-04-07"}))

(deftest new-event-tx
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (event.data/new-event-tx {})))

  (is (s/valid? :bridge/new-event-tx (TEST-NEW-EVENT-TX)))
  (is (= :status/draft (:event/status (TEST-NEW-EVENT-TX)))))

(deftest save-event!

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)]
    (is (event.data/event-id-by-slug new-db TEST-EVENT-SLUG))))
