(ns bridge.ui.util
  (:require [re-frame.core :as rf]))

(def <== (comp deref rf/subscribe))
(def ==> rf/dispatch)

(def log js/console.log)
