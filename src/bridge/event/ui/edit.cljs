(ns bridge.event.ui.edit
  (:require [bridge.event.spec :as event.spec]
            [bridge.event.ui.edit.status :as ui.event.edit.status]
            [bridge.ui.component.date :as ui.date]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==>]]
            [reagent.core :as r]))

(def edit-field
  #:field{:subscription  [:bridge.event.ui/events]
          :commit-action :bridge.event.ui/update-field-value!})

(def TEST-STATUS? false)

(defn edit-event [event-slug]
  (let [*test-status (r/atom :status/draft)]
    (fn []
      (if-some [{:event/keys [slug title status start-date end-date
                              registration-close-date organisers]}
                (get (<== [:bridge.event.ui/events]) [:event/slug event-slug])]

        (let [status     (if TEST-STATUS? @*test-status status)
              edit-field (assoc edit-field :field/entity-id [:event/slug slug])]

          [:div

           (when TEST-STATUS?
             [ui.event.edit.status/test-status-buttons *test-status status])

           [:div.level
            [:div.level-left
             [:div.level-item
              [:h3.title.is-4.spaced "Event details: " [:u title]]]]
            [:div.level-right
             [ui.event.edit.status/edit-event-status edit-field]]]

           [:div.is-divider]

           [ui.event.edit.status/event-status-steps status]

           [:div.is-divider]

           [:div.columns
            [:div.column.is-two-fifths
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

             [:div.field
              [:label.label "Organisers"]
              [:div.control.content
               [:ul
                (for [{:person/keys [name]} organisers]
                  [:li {:key name} name])]]]]

            [:div.is-divider-vertical]

            [:div.column
             [ui.edit-field/edit-text-field
              (merge edit-field
                     #:field{:title       "Details"
                             :attr        :event/details-markdown
                             :type        :markdown
                             :placeholder (event.spec/field->placeholder
                                           :event/details-markdown)})]

             [ui.edit-field/edit-text-field
              (merge edit-field
                     #:field{:title "Notes"
                             :attr  :event/notes-markdown
                             :type  :markdown
                             :placeholder (event.spec/field->placeholder
                                           :event/notes-markdown)})]]]])
        [:p "No event."]))))
