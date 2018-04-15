(ns bridge.event.ui.edit
  (:require [bridge.data.coll :as data.coll]
            [bridge.data.string :as data.string]
            [bridge.event.spec :as event.spec]
            [bridge.ui.component.date :as ui.date]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==>]]
            [reagent.core :as r]))

(defn event-status-steps [current-status cancelled?]
  [:div
   [:div.steps
    (let [current-status-index (data.coll/index-of current-status
                                                   event.spec/status-order)]
      (map-indexed
       (fn [idx status]
         [:div.step-item
          (cond-> {:key (str status)}
            (> current-status-index
               (data.coll/index-of status event.spec/status-order))
            (assoc :class "is-completed")
            (= current-status status)
            (assoc :class "is-active"))
          [:div.step-marker (if (= status :status/complete)
                              [:span.icon [:i.fa.fa-flag]]
                              (inc idx))]
          [:div.step-details
           [:p.step-title (data.string/keyword->label status)]]])
       event.spec/status-order))]
   [:p.has-text-centered (get event.spec/status->description current-status)]])

(defn edit-event-status [current-status cancelled?]
  (when-not cancelled?
    [:div.level-item [:strong "This event is cancelled."]]
    (when-some [[next-status may-cancel?]
                (get event.spec/status->valid-next-status current-status)]
      [:div.level-item.buttons
       [:button.button.is-primary
        (data.string/keyword->label (event.spec/status->active-verb next-status))]
       (when (some? may-cancel?)
         [:button.button.is-warning
          "Cancel event"])])))

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
              cancelled? (= :status/cancelled status)
              edit-field (assoc edit-field :field/entity-id [:event/slug slug])]

          [:div
           (when TEST-STATUS?
             [:div.buttons
              (for [s (conj event.spec/status-order
                            :status/cancelled)]
                [:button.button.is-small {:on-click #(reset! *test-status s)
                                          :class (if (= s status)
                                                   "is-primary"
                                                   "is-warning")}
                 (data.string/keyword->label s)])])
           [:div.level
            [:div.level-left
             [:div.level-item
              [:h3.title.is-4.spaced "Event details: " [:u title]]]]
            [:div.level-right
             [edit-event-status status cancelled?]]]

           [:div.is-divider]

           [event-status-steps status cancelled?]
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
              (merge edit-field #:field{:title "Details"
                                        :attr  :event/details-markdown
                                        :type  :markdown})]

             [ui.edit-field/edit-text-field
              (merge edit-field #:field{:title "Notes"
                                        :attr  :event/notes-markdown
                                        :type  :markdown})]]]])
        [:p "No event."]))))
