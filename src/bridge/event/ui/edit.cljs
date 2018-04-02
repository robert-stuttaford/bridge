(ns bridge.event.ui.edit
  (:require [bridge.ui.component.date :as ui.date]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==>]]
            [reagent.core :as r]))

(def edit-field
  #:field{:subscription  [:bridge.event.ui/events]
          :commit-action :bridge.event.ui/update-field-value!})

(defn edit-event [event-slug]
  (if-some [{:event/keys [slug title status start-date end-date
                          registration-close-date organisers
                          details-markdown notes-markdown]}
            (get (<== [:bridge.event.ui/events]) [:event/slug event-slug])]

    (let [edit-field (assoc edit-field :field/entity-id [:event/slug slug])]

      [:div.column.is-two-fifths
       [:h3.title.is-4.spaced "Event details: " [:u title]]

       [ui.edit-field/edit-text-field
        (merge edit-field #:field{:title "Title"
                                  :attr  :event/title})]

       [ui.edit-field/edit-text-field
        (merge edit-field #:field{:title "Slug"
                                  :attr  :event/slug})]

       [:div.field
        [:label.label "Dates"]
        [:div.control
         [ui.date/select-dates (r/atom {})
          :event/start-date start-date
          :event/end-date end-date]]]

       [:div.field
        [:label.label "Registration Closes"]
        [:div.control
         [ui.date/select-date (r/atom {})
          :event/registration-close-date registration-close-date]]]

       [:hr]

       [:pre "status: " (pr-str status)]
       [:pre "organisers: " (pr-str organisers)]
       [:pre "details-markdown: " (pr-str details-markdown)]
       [:pre "notes-markdown: " (pr-str notes-markdown)]])
    [:p "No event."]))
