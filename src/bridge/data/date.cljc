(ns bridge.data.date
  #?@
  (:clj
   [(:require [clj-time.coerce :as tc] [clj-time.core :as t])]
   :cljs
   [(:require [goog.date.Date :as goog-date])]))

#?(:clj
   (defn date-after? [this that]
     (t/after? (tc/from-date this) (tc/from-date that)))
   :cljs
   (defn date-after? [this that]
     (pos? (goog-date/compare this that))))
