(ns bridge.event.ui.edit.status
  (:require [bridge.data.coll :as data.coll]
            [bridge.data.string :as data.string]
            [bridge.event.spec :as event.spec]
            [bridge.ui.component.modal :as ui.modal]
            [bridge.ui.util :refer [<== ==> log]]
            [reagent.core :as r]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Alter status

(defn set-new-status! [slug status]
  (==> [:bridge.event.ui/update-field-value!
        #:field{:attr  :event/status
                :value status}]))

(defn confirm-change-event-status-modal [*confirm-next-status slug]
  (when-some [next-status @*confirm-next-status]
    (let [close-confirm-modal-fn #(reset! *confirm-next-status nil)
          status-verb            (event.spec/status->active-verb next-status)]
      [ui.modal/warning-modal {:is-active?-fn #(some? @*confirm-next-status)
                               :close!-fn     close-confirm-modal-fn}
       [:div.content
        [:p "You're planning to: " [:u status-verb]]
        [:p "Changing this event's status is permanent!"]
        [:p "Are you sure?"]]
       [:div.is-divider {:style {:border-top ".1rem solid #4a4a4a"}}]
       [:div.buttons
        [:button.button.is-danger {:on-click #(do (close-confirm-modal-fn)
                                                  (set-new-status! slug next-status))}
         "Yes - " status-verb]
        [:button.button.is-text {:on-click close-confirm-modal-fn}
         "No - cancel"]]])))

(defn edit-event-status []
  (let [*confirm-next-status (r/atom nil)]
    (fn []
      (let [{:event/keys [slug status]} (<== [:bridge.event.ui/event-for-editing])]
        (when-not (= :status/cancelled status)
          [:div.level-item [:strong "This event is cancelled."]]
          (when-some [[next-status cancel-status]
                      (get event.spec/status->valid-next-status status)]
            [:div.level-item.buttons
             [:button.button.is-primary
              {:on-click #(reset! *confirm-next-status next-status)}
              (event.spec/status->active-verb next-status)]
             (when (some? cancel-status)
               [:button.button.is-warning
                {:on-click #(reset! *confirm-next-status cancel-status)}
                (event.spec/status->active-verb cancel-status)])
             [confirm-change-event-status-modal *confirm-next-status slug]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Status progression

(defn event-status-steps [current-status]
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
                              [:span.icon [:i.fas.fa-flag]]
                              (inc idx))]
          [:div.step-details
           [:p.step-title (data.string/keyword->label status)]]])
       event.spec/status-order))]
   [:p.has-text-centered (get event.spec/status->description current-status)]])
