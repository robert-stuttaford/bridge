(ns bridge.ui.spec
  (:require bridge.spec
            bridge.event.spec
            bridge.person.spec
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))

(s/def :bridge.ui/active-person
  (s/keys :req [:person/name]))

(s/def :bridge.ui/active-chapter :bridge.datomic/ref)

(s/def :bridge.ui/view keyword?)
(s/def :bridge.ui/params (s/nilable (s/map-of keyword? string?)))

(s/def :bridge.ui/current-view
  (s/keys :req-un [:bridge.ui/view :bridge.ui/params]))

(s/def :bridge.event.ui/events
  (s/map-of :bridge.datomic/ref :bridge/event))

(s/def :bridge.ui/db
  (s/keys :req [:bridge.ui/active-person
                :bridge.ui/current-view]
          :opt [:bridge.ui/active-chapter
                :bridge.event.ui/events]))

(defn check-spec-error [spec value]
  (when-not (s/valid? spec value)
    (s/explain spec value)
    (throw (ex-info (str "Invalid data for spec " (str spec))
                    (s/explain-data spec value)))))

(def check-spec-interceptor
  (rf/after (partial check-spec-error :bridge.ui/db)))
