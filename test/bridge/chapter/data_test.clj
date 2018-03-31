(ns bridge.chapter.data-test
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.chapter.schema :as chapter.schema]
            [bridge.data.slug :as slug]
            [bridge.test.fixtures :as fixtures :refer [TEST-PERSON-ID]]
            [bridge.test.util :refer [conn db test-setup with-database]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is join-fixtures use-fixtures]])
  (:import clojure.lang.ExceptionInfo))

(def db-name (str *ns*))

(use-fixtures :once test-setup)
(use-fixtures :each (join-fixtures [(with-database db-name chapter.schema/schema)
                                    (fixtures/person-fixtures db-name)]))

(def TEST-CHAPTER-TITLE "ClojureBridge Hermanus")
(def TEST-CHAPTER-SLUG (slug/->slug TEST-CHAPTER-TITLE))

(defn TEST-NEW-CHAPTER-TX
  "A function, so that spec instrumentation has a chance to work"
  []
  (chapter.data/new-chapter-tx TEST-PERSON-ID
                               #:chapter{:title TEST-CHAPTER-TITLE
                                         :location "Hermanus"}))

(deftest new-chapter-tx
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (chapter.data/new-chapter-tx {})))

  (is (s/valid? :bridge/new-chapter-tx (TEST-NEW-CHAPTER-TX)))
  (is (= :status/active (:chapter/status (TEST-NEW-CHAPTER-TX)))))

(deftest save-chapter!

  (chapter.data/save-new-chapter! (conn db-name) (TEST-NEW-CHAPTER-TX))

  (let [new-db (db db-name)]
    (is (chapter.data/chapter-id-by-slug new-db TEST-CHAPTER-SLUG))))

(deftest person-is-organiser?

  (chapter.data/save-new-chapter! (conn db-name) (TEST-NEW-CHAPTER-TX))

  (let [new-db (db db-name)]
    (is (true? (chapter.data/person-is-organiser?
                new-db
                (chapter.data/chapter-id-by-slug new-db TEST-CHAPTER-SLUG)
                TEST-PERSON-ID)))

    (is (false? (chapter.data/person-is-organiser?
                 new-db
                 (chapter.data/chapter-id-by-slug new-db TEST-CHAPTER-SLUG)
                 123)))))

(deftest check-chapter-organiser

  (chapter.data/save-new-chapter! (conn db-name) (TEST-NEW-CHAPTER-TX))

  (let [new-db (db db-name)]
    (is (nil? (chapter.data/check-chapter-organiser
               new-db
               (chapter.data/chapter-id-by-slug new-db TEST-CHAPTER-SLUG)
               TEST-PERSON-ID)))

    (is (= {:error :bridge.error/not-chapter-organiser}
           (chapter.data/check-chapter-organiser
            new-db
            (chapter.data/chapter-id-by-slug new-db TEST-CHAPTER-SLUG)
            123)))))
