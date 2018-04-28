(ns bridge.ui.component.modal)

(defn modal [{:keys [is-active?-fn close!-fn]} content]
  [:div#modal.modal (when (is-active?-fn) {:class "is-active"})
   [:div.modal-background]
   [:div.modal-content content]
   [:button.modal-close.is-large {:on-click   close!-fn
                                  :aria-label "close"}]])

(defn warning-modal [modal-options & content]
  [modal modal-options
   [:div.notification.is-warning
    [:article.media
     [:figure.media-left
      [:p.image.is-64x64
       ;; FIX this icon isn't positioned in the image bounds
       [:span.icon
        [:i.fas.fa-3x.fa-exclamation-circle]]]]
     [:div.media-content content]]]])
