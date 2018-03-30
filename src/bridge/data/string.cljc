(ns bridge.data.string
  (:require [clojure.string :as str]))

(defn not-blank [s]
  (when-not (str/blank? s)
    s))
