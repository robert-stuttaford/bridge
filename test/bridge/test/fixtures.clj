(ns bridge.test.fixtures
  (:require [bridge.chapter.data :as chapter.data]
            [bridge.chapter.schema :as chapter.schema]
            [bridge.data.dev-data :as dev-data]
            [bridge.person.data :as person.data]
            [bridge.person.schema :as person.schema]
            [bridge.test.util :refer [conn]]
            [datomic.api :as d]))

(def db-name (str *ns*))

(defn person-fixtures [db-name]
  (fn [test-fn]
    (let [conn (conn db-name)]
      @(d/transact conn person.schema/schema)

      (dev-data/add-person! conn
                            #:person{:name     "Test Name"
                                     :email    "test@cb.org"
                                     :password "secret"}))

    (test-fn)))

(def TEST-PERSON-ID  [:person/email "test@cb.org"])

(defn chapter-fixtures [db-name]
  (fn [test-fn]
    (let [conn (conn db-name)]
      @(d/transact conn chapter.schema/schema)

      (dev-data/add-chapter! conn [:person/email "test@cb.org"]
                             #:chapter{:title    "ClojureBridge Hermanus"
                                       :location "Hermanus"}))

    (test-fn)))

(def TEST-CHAPTER-ID [:chapter/slug "clojurebridge-hermanus"])
