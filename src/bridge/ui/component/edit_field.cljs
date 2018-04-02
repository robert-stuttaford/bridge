(ns bridge.ui.component.edit-field
  (:require [bridge.ui.util :refer [<== ==>]]
            [clojure.spec.alpha :as s]
            [reagent.core :as r]))

(defn reset-edit-state! [*edit value]
  (reset! *edit {:edit-value value}))

(defn update-edit-state! [*edit attr orig-value edit-value]
  (reset! *edit {:edit-value edit-value
                 :dirty?     (not= orig-value edit-value)
                 :invalid?   (if-some [spec (s/get-spec attr)]
                               (not (s/valid? spec edit-value))
                               false)}))

(defn commit-edit! [*edit {:field/keys [commit-action] :as field} edit-value]
  (reset-edit-state! *edit edit-value)
  (==> [commit-action
        (-> field
            (select-keys [:field/entity-id :field/attr])
            (assoc :field/value edit-value))]))

(defn edit-text-field [{:field/keys [subscription entity-id attr title] :as field}]
  (let [*edit (r/atom {})]
    (reset-edit-state! *edit (get-in (<== subscription) [entity-id attr]))
    (fn []
      (let [value (get-in (<== subscription) [entity-id attr])
            {:keys [edit-value dirty? invalid?]} @*edit]

        [:div.field
         [:label.label title]
         [:div.control
          (when invalid? {:class "has-icons-right"})
          [:input.input
           (cond-> {:type        "text"
                    :placeholder (str title (when invalid? " is required!"))
                    :value       edit-value
                    :on-change   #(update-edit-state! *edit attr value
                                                      (.. % -currentTarget -value))
                    :on-key-up   (fn [e]
                                   (condp = (.-key e)
                                     "Enter"  (when-not invalid?
                                                (commit-edit! *edit field edit-value))
                                     "Escape" (when dirty?
                                                (reset-edit-state! *edit value))
                                     nil))}
             dirty?   (assoc :class "is-warning")
             invalid? (assoc :class "is-danger"))]
          (when invalid?
            [:span.icon.is-small.is-right [:i.fa.fa-warning]])]
         (when dirty?
           [:div.buttons.is-pulled-right {:style {:margin-top "5px"}}
            [:button.button.is-small.is-primary
             (if invalid?
               {:disabled "disabled"}
               {:on-click #(commit-edit! *edit field edit-value)})
             "Save"]
            [:button.button.is-small.is-text
             {:on-click #(reset-edit-state! *edit value)}
             "Cancel"]])]))))
