(ns bridge.event.spec
  (:require bridge.spec
            [clojure.spec.alpha :as s]))

(s/def :event/status #{:status/draft :status/published :status/in-progress
                       :status/cancelled :status/complete})
(s/def :event/title :bridge.spec/required-string)
(s/def :event/slug :bridge.spec/slug)
(s/def :event/chapter :bridge.datomic/ref)
(s/def :event/organisers (s/coll-of :bridge.datomic/ref :min-count 1))
(s/def :event/start-date inst?)
(s/def :event/end-date inst?)
(s/def :event/registration-close-date inst?)
(s/def :event/details-markdown :bridge.spec/optional-string)
(s/def :event/notes-markdown :bridge.spec/optional-string)

(s/def :bridge/new-event
  (s/keys :req [:event/title :event/start-date :event/end-date]
          :opt [:event/registration-close-date]))

(s/def :bridge/event
  (s/merge :bridge/new-event
           (s/keys :req [:event/status :event/slug :event/registration-close-date
                         :event/chapter :event/organisers]
                   :opt [:event/details-markdown :event/notes-markdown])))

(s/def :bridge/new-event-tx :bridge/event)
