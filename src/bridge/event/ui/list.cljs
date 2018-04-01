(ns bridge.event.ui.list
  (:require [bridge.data.date :as data.date]
            [bridge.data.string :as data.string]
            [bridge.ui.base :as ui.base]
            [bridge.ui.routes :as ui.routes]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn load-events-for-active-chapter []
  (rf/dispatch [:bridge.event.ui/list-events-for-chapter
                @(rf/subscribe [:bridge.ui/active-chapter])]))

(defn list-events []
  (r/create-class
   {:display-name        "list-events"
    :component-did-mount (fn [_]
                           (load-events-for-active-chapter))
    :reagent-render
    (fn [_]
      [:div.column.is-two-fifths
       [:h3.title.is-4.spaced "All Events"]
       [:a.button {:href     "javascript:"
                   :on-click #(load-events-for-active-chapter)}
        "Refresh"]

       (for [{:event/keys [title slug status start-date end-date
                           registration-close-date]}
             (vals @(rf/subscribe [:bridge.event.ui/events]))]

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
            [:strong [data.date/date-time registration-close-date]]]]
          [:footer.card-footer
           [:a.card-footer-item (ui.routes/turbo-links (str "/app/events/edit/" slug))
            "Edit"]]])])}))

(defmethod ui.base/view :list-events [_]
  [list-events])
