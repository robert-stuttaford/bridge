(ns bridge.event.data.edit-test
  (:require [bridge.data.slug :as slug]
            [bridge.event.data :as event.data]
            [bridge.event.schema :as event.schema]
            [bridge.event.data.edit :as event.data.edit]
            [bridge.test.fixtures :as fixtures :refer [TEST-CHAPTER-ID TEST-PERSON-ID]]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]]))

(def db-name (str *ns*))

(use-fixtures :once test-setup)
(use-fixtures :each (join-fixtures [(with-database db-name event.schema/schema)
                                    (fixtures/person-fixtures db-name)
                                    (fixtures/chapter-fixtures db-name)]))

(def TEST-EVENT-TITLE "April Event")
(def TEST-EVENT-SLUG (slug/->slug TEST-EVENT-TITLE))

(defn TEST-NEW-EVENT-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (event.data/new-event-tx TEST-CHAPTER-ID TEST-PERSON-ID
                           #:event{:title      TEST-EVENT-TITLE
                                   :start-date #inst "2018-04-06"
                                   :end-date   #inst "2018-04-07"}))

(deftest event-for-editing

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)
        for-editing (->> TEST-EVENT-SLUG
                         (event.data/event-id-by-slug new-db)
                         (event.data.edit/event-for-editing new-db))]
    (is (= (get-in for-editing [:event/chapter :chapter/slug])
           "clojurebridge-hermanus"))
    (is (= (->  for-editing :event/organisers first :person/name)
           "Test Name"))
    ))

;; TODO test custom validations - status, organisers
