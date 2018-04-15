(ns bridge.event.spec
  (:require [bridge.data.date :as data.date]
            bridge.spec
            bridge.data.datomic.spec
            [clojure.spec.alpha :as s]))

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

;;; UI strings

(def status-order
  [:status/draft
   :status/registering
   :status/inviting
   :status/in-progress
   :status/complete])

(def status->active-verb
  {:status/registering "Open Registration"
   :status/inviting    "Close Registration & Send Invites"
   :status/in-progress "Begin Event"
   :status/complete    "Finish Event"
   :status/cancelled   "Cancel Event"})

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

(def field->placeholder
  {:event/details-markdown
   "Tell folks about this event:

Include start time, information about the location(s), where to park, secret passphrase to use at the door, etc.

Markdown syntax is supported."
   :event/notes-markdown
   "Private notes for organisers to share.

Markdown syntax is supported."})
