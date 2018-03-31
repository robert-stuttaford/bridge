(ns bridge.data.edit-test
  (:require [bridge.data.datomic :as datomic]
            [bridge.data.edit :as data.edit]
            [bridge.data.slug :as slug]
            [bridge.event.data :as event.data]
            [bridge.event.schema :as event.schema]
            [bridge.test.fixtures
             :as
             fixtures
             :refer
             [TEST-CHAPTER-ID TEST-PERSON-ID]]
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

(deftest check-custom-validation
  (is (thrown-with-msg? ExceptionInfo #"did not conform to spec"
                        (data.edit/check-custom-validation* (db db-name)
                                                           {})))

  (is (nil? (data.edit/check-custom-validation* (db db-name)
                                               #:field{:entity-id 1
                                                       :attr      :keyword
                                                       :value     1}))))

(defmethod data.edit/check-custom-validation :test-custom-pass [_ _])

(deftest check-custom-validation-specific

  (is (nil? (data.edit/check-custom-validation* (db db-name)
                                               #:field{:entity-id 1
                                                       :attr      :test-custom-pass
                                                       :value     1}))))

(defmethod data.edit/check-custom-validation :test-custom-incorrect-error-data [_ _]
  {:result :fail})

(deftest check-custom-validation-specific
  (is (thrown-with-msg?
       ExceptionInfo #"did not conform to spec"
       (data.edit/check-custom-validation*
        (db db-name)
        #:field{:entity-id 1
                :attr      :test-custom-incorrect-error-data
                :value     1}))))

(defmethod data.edit/check-custom-validation :test-custom-correct-error-data [_ _]
  {:error :bridge.error/fail})

(defn is-error? [k m]
  (= k (:error m)))

(deftest check-custom-validation-specific
  (is (is-error? :bridge.error/fail
                 (data.edit/check-custom-validation*
                  (db db-name)
                  #:field{:entity-id 1
                          :attr      :test-custom-correct-error-data
                          :value     1}))))

(s/def :fake/attr string?)

(deftest check-field-update:invalid-edit-key
  (is (is-error? :bridge.error/invalid-edit-key
                 (data.edit/check-field-update:invalid-edit-key
                  (db db-name)
                  #{}
                  #:field{:entity-id 1
                          :attr      :test-whitelist
                          :value     1})))

  (is (nil? (data.edit/check-field-update:invalid-edit-key
             (db db-name)
             #{:test-whitelist}
             #:field{:entity-id 1
                     :attr      :test-whitelist
                     :value     1}))))

(deftest check-field-update:invalid-edit-value
  (is (is-error? :bridge.error/invalid-edit-value
                 (data.edit/check-field-update:invalid-edit-value
                  (db db-name)
                  #:field{:entity-id 1
                          :attr      :fake/attr
                          :value     1})))

  (is (nil? (data.edit/check-field-update:invalid-edit-value
             (db db-name)
             #:field{:entity-id 1
                     :attr      :fake/attr
                     :value     "one"}))))

(deftest check-field-update:uniqueness-conflict
  (is (is-error? :bridge.error/uniqueness-conflict
                 (data.edit/check-field-update:uniqueness-conflict
                  (db db-name)
                  #:field{:entity-id 1
                          :attr      :chapter/slug
                          :value     (second TEST-CHAPTER-ID)})))

  (is (nil? (data.edit/check-field-update:uniqueness-conflict
             (db db-name)
             #:field{:entity-id 1
                     :attr      :chapter/slug
                     :value     "something-else"}))))

(deftest check-field-update:missing-referent
  (is (is-error? :bridge.error/missing-referent
                 (data.edit/check-field-update:missing-referent
                  (db db-name)
                  #:field{:entity-id 1
                          :attr      :event/organisers
                          :value     [:person/email "not@here.org"]})))

  (is (nil? (data.edit/check-field-update:missing-referent
             (db db-name)
             #:field{:entity-id 1
                     :attr      :event/organisers
                     :value     TEST-PERSON-ID}))))

(deftest check-field-update-reaches-custom
  (is (is-error? :bridge.error/fail
                 (data.edit/check-field-update
                  (db db-name)
                  #{:test-custom-correct-error-data}
                  #:field{:entity-id 1
                          :attr      :test-custom-correct-error-data
                          :value     1}))))

(deftest update-field-value-tx
  (is (= [:db/add 1 :event/title "title"]
         (data.edit/update-field-value-tx
          (db db-name)
          #:field{:entity-id 1
                  :attr      :event/title
                  :value     "title"})))

  (is (= [:db/retract [:event/slug "test-event"] :attr "abc"]
         (data.edit/update-field-value-tx
          (db db-name)
          #:field{:entity-id [:event/slug "test-event"]
                  :attr      :attr
                  :value     "abc"
                  :retract?  true})))

  (let [_ (datomic/transact! (conn db-name)
                             [{:db/id                "tempid"
                               :event/slug           "test-event"
                               :event/notes-markdown "# notes"}])]
    (is (= [:db/retract [:event/slug "test-event"] :event/notes-markdown "# notes"]
           (data.edit/update-field-value-tx
            (db db-name)
            #:field{:entity-id [:event/slug "test-event"]
                    :attr      :event/notes-markdown
                    :value     ""})))))

(deftest update-field-value!
  (let [_ (datomic/transact! (conn db-name)
                             [{:db/id      "tempid"
                               :event/slug "test-event"}])
        _ (data.edit/update-field-value!
           (conn db-name)
           (db db-name)
           #{:event/notes-markdown}
           #:field{:entity-id [:event/slug "test-event"]
                   :attr      :event/notes-markdown
                   :value     "# notes"})]
    (is (= (datomic/attr (db db-name) [:event/slug "test-event"] :event/notes-markdown)
           "# notes")))

  (let [_ (datomic/transact! (conn db-name)
                             [{:db/id      "tempid"
                               :event/slug "test-event2"}])
        _ (data.edit/update-field-value!
           (conn db-name)
           (db db-name)
           #{:event/notes-markdown}
           #:field{:entity-id [:event/slug "test-event"]
                   :attr      :event/notes-markdown
                   :value     ""})]
    (is (nil? (datomic/attr (db db-name) [:event/slug "test-event"] :event/notes-markdown)))))