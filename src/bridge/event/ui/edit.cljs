(ns bridge.event.ui.edit
  (:require [bridge.data.date :as data.date]
            [bridge.data.string :as data.string]
            [bridge.ui.base :as ui.base]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn edit-event [event-slug]
  (if-some [{:event/keys [title slug status start-date end-date
                          registration-close-date organisers
                          details-markdown notes-markdown]}
            (get @(rf/subscribe [:bridge.event.ui/events]) event-slug)]
    [:div.column.is-two-fifths
     [:h3.title.is-4.spaced "Edit event"]

     [:div.card {:key   slug
                 :style {:margin-top "2rem"}}
      [:header.card-header
       [:p.card-header-title title " (" (data.string/keyword->label status) ")"]]
      [:div.card-content
       [:div.content
        [:strong [data.date/date-time start-date]] " - "
        [:strong [data.date/date-time end-date]]
        [:br]
        "Registration ends "
        [:strong [data.date/date-time registration-close-date]]

        [:pre "organisers: " (pr-str organisers)]
        [:pre "details-markdown: " (pr-str details-markdown)]
        [:pre "notes-markdown: " (pr-str notes-markdown)]]]]]
    [:p "No event."]))

(defmethod ui.base/load-on-view :edit-event [{{:keys [event-slug]} :params}]
  [:bridge.event.ui/event-for-editing [:event/slug event-slug]])

(defmethod ui.base/view :edit-event [{{:keys [event-slug]} :params}]
  [edit-event event-slug])
