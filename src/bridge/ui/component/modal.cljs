(ns bridge.ui.component.modal)

(defn modal [{:keys [is-active?-fn close!-fn]} content]
  [:div#modal.modal (when (is-active?-fn) {:class "is-active"})
   [:div.modal-background]
   [:div.modal-content content]
   [:button.modal-close.is-large {:on-click   close!-fn
                                  :aria-label "close"}]])
