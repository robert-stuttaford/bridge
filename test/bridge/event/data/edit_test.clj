(ns bridge.event.data.edit-test
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.edit :as data.edit]
            [bridge.data.slug :as slug]
            [bridge.event.data :as event.data]
            [bridge.event.data.edit :as event.data.edit]
            [bridge.event.schema :as event.schema]
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
           "Test Name"))))

(deftest check-custom-validation:event-status

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (is (= :bridge.event.error/invalid-next-status
         (:error (data.edit/check-custom-validation
                  (db db-name)
                  #:field{:entity-id [:event/slug TEST-EVENT-SLUG]
                          :attr      :event/status
                          :value     :event/complete}))))

  (datomic/transact! (conn db-name) [[:db/add [:event/slug TEST-EVENT-SLUG]
                                      :event/status :status/complete]])

  (is (= :bridge.event.error/status-may-not-change
         (:error (data.edit/check-custom-validation
                  (db db-name)
                  #:field{:entity-id [:event/slug TEST-EVENT-SLUG]
                          :attr      :event/status
                          :value     :event/complete})))))

(deftest check-custom-validation:event-organisers

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (is (= :bridge.event.error/event-can-not-have-no-organisers
         (:error (data.edit/check-custom-validation
                  (db db-name)
                  #:field{:entity-id [:event/slug TEST-EVENT-SLUG]
                          :attr      :event/organisers
                          :value     1
                          :retract?  true})))))
