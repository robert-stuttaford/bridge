(ns bridge.event.ui.list
  (:require [bridge.data.date :as data.date]
            [bridge.data.string :as data.string]
            [bridge.ui.base :as ui.base]
            [bridge.ui.routes :as ui.routes]
            [bridge.ui.util :refer [<== ==> log]]))

(defn list-events []
  [:div
   [:div.level
    [:div.level-left
     [:div.level-item
      [:h3.title.is-4.spaced "All Events"]]]
    [:div.level-right
     [:a.button {:href     "javascript:"
                 :on-click #(==> (ui.base/load-on-view {:view :list-events}))}
      "Refresh"]]]

   (let [events (<== [:bridge.event.ui/events])]
     (if (empty? events)

       [:section.hero
        [:div.hero-body
         [:div.container
          [:h1.title "No events yet"]
          [:br]
          [:h2.subtitle "Go ahead and "
           [:a (ui.routes/turbolink :create-event) "create your first event"]
           " now!"]]]]

       [:div.columns.is-multiline
        (for [{:event/keys [title slug status start-date end-date
                            registration-close-date]} (vals events)]
          [:div.column.is-one-third {:key slug}
           [:div.card {:style {:margin-top "2rem"}}
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
             [:a.card-footer-item (ui.routes/turbolink :edit-event {:event-slug slug})
              "Edit"]]]])]))])
