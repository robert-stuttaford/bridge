(ns bridge.event.spec
  (:require [bridge.data.date :as data.date]
            bridge.spec
            bridge.data.datomic.spec
            [clojure.spec.alpha :as s]))

(def status-order
  [:status/draft
   :status/registering
   :status/inviting
   :status/in-progress
   :status/complete])

(def status->active-verb
  {:status/registering "Open registration"
   :status/inviting    "Close registration & send invites"
   :status/in-progress "Begin event"
   :status/complete    "Finish event"})

(def status->description
  {:status/draft       "This event is not yet public."
   ;; TODO link to registration form
   :status/registering "Participants may register."
   :status/inviting    "Registration is closed; awaiting confirmed invitations."
   :status/in-progress "This event is happening right now!"
   :status/complete    "This event is all done!"
   :status/cancelled   "This event is cancelled."})

(def status->valid-next-status
  {:status/draft       [:status/registering :status/cancelled]
   :status/registering [:status/inviting    :status/cancelled]
   :status/inviting    [:status/in-progress :status/cancelled]
   :status/in-progress [:status/complete    :status/cancelled]})

(s/def :event/status #{:status/draft :status/registering :status/inviting
                       :status/in-progress :status/cancelled :status/complete})
(s/def :event/title :bridge.spec/required-string)
(s/def :event/slug :bridge.spec/slug)
(s/def :event/chapter :bridge.datomic/ref)
(s/def :event/organisers
  (s/or :ref :bridge.datomic/ref
        :ref-coll (s/coll-of :bridge.datomic/ref :min-count 1)))
(s/def :event/start-date inst?)
(s/def :event/end-date inst?)
(s/def :event/registration-close-date inst?)
(s/def :event/details-markdown :bridge.spec/optional-string)
(s/def :event/notes-markdown :bridge.spec/optional-string)

(defn event-dates-in-order? [{:event/keys [start-date
                                           end-date
                                           registration-close-date]}]
  (and (not (data.date/date-after? start-date
                                   end-date))
       (or (nil? registration-close-date)
           (not (data.date/date-after? registration-close-date
                                       start-date)))))

(s/def :bridge/new-event
  (s/and (s/keys :req [:event/title :event/start-date :event/end-date]
                 :opt [:event/registration-close-date])
         event-dates-in-order?))

(s/def :bridge/event
  (s/merge :bridge/new-event
           (s/keys :req [:event/status :event/slug :event/registration-close-date
                         :event/chapter :event/organisers]
                   :opt [:event/details-markdown :event/notes-markdown])))

(s/def :bridge/new-event-tx :bridge/event)
