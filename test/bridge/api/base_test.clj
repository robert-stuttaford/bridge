(ns bridge.event.api-test
  (:require [bridge.test.util :refer [test-setup]]
            [bridge.web.api.base :as api.base]
            [clojure.test :refer [deftest use-fixtures]])
  (:import clojure.lang.ExceptionInfo))

(use-fixtures :once test-setup)

(deftest api-base
  (is (thrown-with-msg? ExceptionInfo #"API action not supported"
                        (api.base/api {:action :not-implemented}))))

(defmethod api.base/api ::implemented [params] :implemented)

(deftest api-extension
  (is (= :implemented (api.base/api {:action ::implemented}))))

(deftest request->api-payload
  (is (= {:action           :action
          :datomic/db       1
          :datomic/conn     1
          :active-person-id 1}
         (api.base/request->api-payload {:session      {:identity 1}
                                         :datomic/db   1
                                         :datomic/conn 1
                                         :body         "{:action :action}"}))))

(deftest new-payload
  (is (= {:action           :new-action
          :datomic/db       2
          :datomic/conn     1
          :active-person-id 1}
         (api.base/new-payload {:action           :action
                                :datomic/db       1
                                :datomic/conn     1
                                :active-person-id 1}
                               {:action     :new-action
                                :datomic/db 2}))))
