(ns bridge.event.ui.edit
  (:require [bridge.event.spec :as event.spec]
            [bridge.event.ui.edit.status :as ui.event.edit.status]
            [bridge.ui.component.date :as ui.date]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==> log]]))

(defn edit-event [event-slug]
  (if-some [{:event/keys [title status organisers]}
            (<== [:bridge.event.ui/event-for-editing])]

    [:div

     [:div.level
      [:div.level-left
       [:div.level-item
        [:h3.title.is-4.spaced "Event details: " [:u (:orig-value title)]]]]
      [:div.level-right
       [ui.event.edit.status/edit-event-status]]]

     [:div.is-divider]

     [ui.event.edit.status/event-status-steps status]

     [:div.is-divider]

     [:div.columns
      [:div.column.is-two-fifths
       [ui.edit-field/edit (event.spec/attr->field :event/title)]
       [ui.edit-field/edit (event.spec/attr->field :event/slug)]

       [:div.field
        [:label.label "Dates"]
        [:div.control
         ;; TODO clamp registration-closes to <= start-date
         [ui.date/select-dates
          #(get (<== [:bridge.event.ui/event-for-editing]) :event/start-date)
          #(get (<== [:bridge.event.ui/event-for-editing]) :event/end-date)
          #(do (==> [:bridge.event.ui/update-field-value! :event/start-date (:start %)])
               (==> [:bridge.event.ui/update-field-value! :event/end-date (:end %)]))]]]

       [:div.field
        [:label.label "Registration Closes"]
        [:div.control
         ;; TODO prevent later than start-date
         [ui.date/select-date
          #(get (<== [:bridge.event.ui/event-for-editing])
                :event/registration-close-date)
          #(==> [:bridge.event.ui/update-field-value!
                 :event/registration-close-date %])]]]

       [:div.field
        [:label.label "Organisers"]
        [:div.control.content
         [:ul
          (for [{:person/keys [name]} organisers]
            [:li {:key name} name])]]]]

      [:div.is-divider-vertical]

      [:div.column
       [ui.edit-field/edit (event.spec/attr->field :event/details-markdown)]
       [ui.edit-field/edit (event.spec/attr->field :event/notes-markdown)]]]]
    [:p "No event."]))
