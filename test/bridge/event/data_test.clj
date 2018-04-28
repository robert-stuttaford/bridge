(ns bridge.event.data-test
  (:require [bridge.data.slug :as slug]
            [bridge.event.data :as event.data]
            [bridge.event.schema :as event.schema]
            [bridge.test.fixtures :as fixtures :refer [TEST-CHAPTER-ID TEST-PERSON-ID]]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]])
  (:import clojure.lang.ExceptionInfo))

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

(deftest new-event-tx
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (event.data/new-event-tx {})))

  (is (s/valid? :bridge/new-event-tx (TEST-NEW-EVENT-TX)))
  (is (= :status/draft (:event/status (TEST-NEW-EVENT-TX)))))

(deftest save-event!

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)]
    (is (event.data/event-id-by-slug new-db TEST-EVENT-SLUG))))

(deftest person-is-organiser?

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)]
    (is (true? (event.data/person-is-organiser?
                new-db
                (event.data/event-id-by-slug new-db TEST-EVENT-SLUG)
                TEST-PERSON-ID)))

    (is (false? (event.data/person-is-organiser?
                 new-db
                 (event.data/event-id-by-slug new-db TEST-EVENT-SLUG)
                 123)))))

(deftest check-event-organiser

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)]
    (is (nil? (event.data/check-event-organiser
               new-db
               (event.data/event-id-by-slug new-db TEST-EVENT-SLUG)
               TEST-PERSON-ID)))

    (is (= {:error :bridge.error/not-event-organiser}
           (event.data/check-event-organiser
            new-db
            (event.data/event-id-by-slug new-db TEST-EVENT-SLUG)
            123)))))

(deftest event-ids-by-chapter

  (is (= [] (event.data/event-ids-by-chapter (db db-name) TEST-CHAPTER-ID)))

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)]
    (is (= [(event.data/event-id-by-slug new-db TEST-EVENT-SLUG)]
           (event.data/event-ids-by-chapter new-db TEST-CHAPTER-ID)))))

(deftest event-for-listing

  (event.data/save-new-event! (conn db-name) (TEST-NEW-EVENT-TX))

  (let [new-db (db db-name)
        event (->> (event.data/event-id-by-slug new-db TEST-EVENT-SLUG)
                   (event.data/event-for-listing new-db))]
    (is (some? (:event/slug event)))
    (is (some? (get-in event [:event/chapter :chapter/slug])))
    (is (some? (get-in event [:event/organisers 0 :person/name])))))

