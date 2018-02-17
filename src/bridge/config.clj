(ns bridge.config
  (:require [clojure.java.io :as io]
            [com.walmartlabs.dyn-edn :as dyn-edn]
            [integrant.core :as ig]))

(defn system []
  (ig/read-string {:readers (dyn-edn/env-readers)}
                  (slurp (io/resource "system.edn"))))
