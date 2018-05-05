(ns bridge.web.api.base
  (:require [clojure.edn :as edn]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; API implementation

(defmulti api :action)

(defmethod api :default [params]
  (throw (ex-info "API action not supported" params)))

(defmulti api-spec :action)

(defmethod api-spec :default [_] nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; API payloads

(defn request->api-payload [{:keys [body]
                             {:keys [identity]} :session
                             :as request}]
  (merge (edn/read-string (slurp body))
         (select-keys request [:datomic/db :datomic/conn])
         {:active-person-id identity}))

(defn new-payload [orig-payload new-payload]
  (-> orig-payload
      (select-keys [:datomic/db :datomic/conn :active-person-id])
      (merge new-payload)))
