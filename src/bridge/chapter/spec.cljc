(ns bridge.chapter.spec
  (:require bridge.spec
            [clojure.spec.alpha :as s]))

(s/def :chapter/status #{:status/active})
(s/def :chapter/title :bridge.spec/required-string)
(s/def :chapter/slug :bridge.spec/slug)
(s/def :chapter/location :bridge.spec/required-string)
(s/def :chapter/organisers (s/coll-of :bridge.datomic/ref :min-count 1))

(s/def :bridge/new-chapter
  (s/keys :req [:chapter/title :chapter/location]))

(s/def :bridge/chapter
  (s/merge :bridge/new-chapter
           (s/keys :req [:chapter/status :chapter/slug :chapter/organisers]
                   :opt [:chapter/events])))

(s/def :bridge/new-chapter-tx :bridge/chapter)
