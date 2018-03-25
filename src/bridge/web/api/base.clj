(ns bridge.web.api.base
  (:require [clojure.edn :as edn]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; API implementation

(defmulti api :action)

(defmethod api :default [params]
  (throw (ex-info "API action not supported" params)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; API payloads

(defn request->api-payload [{:keys [body]
                             {:keys [identity]} :session
                             :as request}]
  (merge (edn/read-string body)
         (select-keys [:datomic/db :datomic/conn] request)
         {:active-user-id identity}))
