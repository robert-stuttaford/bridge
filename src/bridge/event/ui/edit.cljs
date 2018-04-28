(ns bridge.event.ui.edit
  (:require [bridge.event.spec :as event.spec]
            [bridge.event.ui.edit.status :as ui.event.edit.status]
            [bridge.ui.component.date :as ui.date]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==> log]]))

(def edit-field
  #:field{:subscription  [:bridge.event.ui/events]
          :commit-action :bridge.event.ui/update-field-value!})

(defn edit-event [event-slug]
  (if-some [{:event/keys [slug title status organisers]}
            (get (<== [:bridge.event.ui/events]) [:event/slug event-slug])]

    (let [entity-id [:event/slug slug]
          edit-field (assoc edit-field :field/entity-id entity-id)]

      [:div

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
                                    :attr  :event/title
                                    :type  :text})]

         [ui.edit-field/edit-text-field
          (merge edit-field #:field{:title "Slug"
                                    :attr  :event/slug
                                    :type  :text})]

         [:div.field
          [:label.label "Dates"]
          [:div.control
           ;; TODO clamp registration-closes to <= start-date
           [ui.date/select-dates
            #(get-in (<== [:bridge.event.ui/events]) [entity-id :event/start-date])
            #(get-in (<== [:bridge.event.ui/events]) [entity-id :event/end-date])
            #(do (==> [:bridge.event.ui/update-field-value!
                       #:field{:entity-id entity-id
                               :attr  :event/start-date
                               :value (:start %)}])
                 (==> [:bridge.event.ui/update-field-value!
                       #:field{:entity-id entity-id
                               :attr  :event/end-date
                               :value (:end %)}]))]]]

         [:div.field
          [:label.label "Registration Closes"]
          [:div.control
           ;; TODO prevent later than start-date
           [ui.date/select-date
            #(get-in (<== [:bridge.event.ui/events])
                     [entity-id :event/registration-close-date])
            #(==> [:bridge.event.ui/update-field-value!
                   #:field{:entity-id entity-id
                           :attr  :event/registration-close-date
                           :value %}])]]]

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
                 #:field{:title       "Notes"
                         :attr        :event/notes-markdown
                         :type        :markdown
                         :placeholder (event.spec/field->placeholder
                                       :event/notes-markdown)})]]]])
    [:p "No event."]))
