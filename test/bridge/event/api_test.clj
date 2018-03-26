(ns bridge.event.api-test
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.data.dev-data :as dev-data]
            [bridge.data.slug :as slug]
            [bridge.event.api :as event.api]
            [bridge.event.data :as event.data]
            [bridge.person.data :as person.data]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [bridge.web.api.base :as api.base]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]]
            [datomic.api :as d]))

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

(defn chapter-fixtures [db-name]
  (fn [test-fn]
    (let [conn (conn db-name)]
      @(d/transact conn chapter.data/schema)

      (dev-data/add-chapter! conn [:person/email "test@cb.org"]
                             #:chapter{:title    "ClojureBridge Hermanus"
                                       :location "Hermanus"}))

    (test-fn)))

(use-fixtures :once test-setup)
(use-fixtures :each (join-fixtures [(with-database db-name event.data/schema)
                                    (person-fixtures db-name)
                                    (chapter-fixtures db-name)]))

(def TEST-CHAPTER-ID [:chapter/slug "clojurebridge-hermanus"])
(def TEST-PERSON-ID [:person/email "test@cb.org"])

(def TEST-EVENT-TITLE "April Event")
(def TEST-EVENT-SLUG (slug/->slug TEST-EVENT-TITLE))

(def TEST-NEW-EVENT
  #:event{:title      TEST-EVENT-TITLE
          :start-date #inst "2018-04-06"
          :end-date   #inst "2018-04-07"})

(defn TEST-NEW-EVENT-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (event.data/new-event-tx TEST-CHAPTER-ID TEST-PERSON-ID TEST-NEW-EVENT))

(defn TEST-PAYLOAD []
  {:datomic/db     (db db-name)
   :datomic/conn   (conn db-name)
   :active-user-id TEST-PERSON-ID})

(deftest edit-event

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [result (api.base/api (merge (TEST-PAYLOAD)
                                    {:action     ::event.api/edit-event
                                     :event-slug "april-event"}))]

    (is (= (get-in result [:event/chapter :chapter/slug])
           "clojurebridge-hermanus"))
    (is (= (->  result :event/organisers first :person/name)
           "Test Name"))))

(deftest save-new-event!

  (let [result (api.base/api (merge (TEST-PAYLOAD)
                                    {:action     ::event.api/save-new-event!
                                     :chapter-id TEST-CHAPTER-ID
                                     :new-event  TEST-NEW-EVENT}))]

    (is (= (get-in result [:event/chapter :chapter/slug])
           "clojurebridge-hermanus"))
    (is (= (->  result :event/organisers first :person/name)
           "Test Name"))))
