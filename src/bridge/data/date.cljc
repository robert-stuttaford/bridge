(ns bridge.data.date
  #?@
  (:clj
   [(:require [clj-time.coerce :as tc] [clj-time.core :as t])]
   :cljs
   [(:require [goog.date.Date :as goog-date])
    (:import goog.i18n.DateTimeFormat)]))

#?(:clj
   (defn date-after? [this that]
     (t/after? (tc/from-date this) (tc/from-date that)))
   :cljs
   (defn date-after? [this that]
     (pos? (goog-date/compare this that))))

#?(:cljs
   (defn format-date [fmt date]
     (.format (DateTimeFormat. fmt) date)))

#?(:cljs
   (defn date-formatter [format]
     #(format-date format %)))

#?(:cljs
   (def format-date-day (date-formatter "yyy\u00B7MM\u00B7dd")))

#?(:cljs
   (defn date-time [dt]
     [:time {:dateTime (str dt)} (format-date-day dt)]))
