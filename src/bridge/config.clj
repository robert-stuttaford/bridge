(ns bridge.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.walmartlabs.dyn-edn :as dyn-edn]
            [integrant.core :as ig]))

(def config
  (->> "config.edn"
       io/resource
       slurp
       (edn/read-string {:readers (dyn-edn/env-readers)})))

(defn system []
  (-> "system.edn"
      io/resource
      slurp
      ig/read-string))
