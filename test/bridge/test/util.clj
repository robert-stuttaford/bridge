(ns bridge.test.util
  (:require [bridge.data.datomic :as datomic]
            [clojure.spec.alpha :as s]
            clojure.test
            [datomic.api :as d]
            [orchestra.spec.test :as sot]))

(defn check-asserts [test-fn]
  (let [old-value (s/check-asserts?)]
    (s/check-asserts true)
    (test-fn)
    (s/check-asserts old-value)))

(defn instrument-all [test-fn]
  (sot/instrument)
  (test-fn))

(defn explain-printer [test-fn]
  (let [old-value s/*explain-out*]
    (alter-var-root (var s/*explain-out*) (constantly s/explain-printer))
    (test-fn)
    (alter-var-root (var s/*explain-out*) (constantly old-value))))

(def test-setup
  (clojure.test/join-fixtures [check-asserts
                               instrument-all
                               explain-printer]))

(defn with-database [db-name schema]
  (fn [test-fn]
    (let [uri    (str "datomic:mem://" db-name)
          _      (d/delete-database uri)
          _      (d/create-database uri)
          conn   (d/connect uri)
          _      @(d/transact conn schema)
          result (datomic/with-datomic-mode :peer
                   (test-fn))]
      (d/release conn)
      (d/delete-database uri)
      result)))

(defn conn [db-name]
  (d/connect (str "datomic:mem://" db-name)))

(defn db [db-name]
  (d/db (conn db-name)))
