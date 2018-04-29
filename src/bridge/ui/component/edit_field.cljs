(ns bridge.ui.component.edit-field
  (:require bridge.data.edit.spec
            [bridge.data.string :as data.string]
            [bridge.ui.spec :as ui.spec]
            [bridge.ui.util :refer [<== ==> log]]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [reagent.core :as r]))

(defn reset-edit-state! [type *edit value]
  (reset! *edit {:editing?   (not= type :markdown)
                 :edit-value value}))

(defn update-edit-state! [*edit attr orig-value edit-value]
  (swap! *edit (fn [edit]
                 (merge (select-keys edit [:editing?])
                        {:edit-value edit-value
                         :dirty?     (not= orig-value edit-value)
                         :invalid?   (if-some [spec (s/get-spec attr)]
                                       (not (s/valid? spec edit-value))
                                       false)}))))

(defn commit-edit! [*edit {:field/keys [type commit-action] :as field} edit-value]
  (reset-edit-state! type *edit edit-value)
  (==> [commit-action
        (-> field
            (select-keys [:field/entity-id :field/attr])
            (assoc :field/value edit-value))]))

(defn markdown [content placeholder]
  (let [using-placeholder? (and (str/blank? content)
                                (data.string/not-blank placeholder))]
    [:div.content
     {:style (cond-> {:padding          "1.5rem"
                      :background-color "#eee"
                      :border-radius    "0.8rem"
                      :min-height       "230px"}
               using-placeholder? (assoc :color "#999"))
      :dangerouslySetInnerHTML
      {:__html (or (-> (or (data.string/not-blank content)
                           (data.string/not-blank placeholder))
                       data.string/markdown->html
                       data.string/not-blank)
                   "(empty)")}}]))

(defn edit-text-field [field]
  (ui.spec/check-spec-error :bridge/edit-field-config field)
  (let [{:field/keys [type subscription attr title placeholder]} field
        *edit (r/atom {})]
    (reset-edit-state! type *edit (get (<== subscription) attr))
    (fn []
      (let [value (get (<== subscription) attr)
            {:keys [editing? edit-value dirty? invalid?]} @*edit]
        [:div.field
         [:label.label [:u title] (when-not editing? " (click to edit)")]
         (if editing?
           [:div.control
            (when invalid? {:class "has-icons-right"})
            [(case type
               (:text :email) :input.input
               :markdown      :textarea.textarea)
             (cond-> {:placeholder (str (or placeholder title)
                                        (when invalid? " is required!"))
                      :value       edit-value
                      :on-change   #(update-edit-state! *edit attr value
                                                        (.. % -currentTarget -value))
                      :on-key-up
                      (fn [e]
                        (.stopImmediatePropagation (aget e "nativeEvent"))
                        (condp = (.-key e)
                          "Enter"  (when (and (not invalid?)
                                              (or (not= type :markdown)
                                                  (.-ctrlKey e)))
                                     (.preventDefault e)
                                     (commit-edit! *edit field edit-value))
                          "Escape" (reset-edit-state! type *edit value)
                          nil))}
               (not= type :markdown) (assoc :type (name type))
               (= type :markdown)    (merge {:rows      10
                                             :autoFocus "autofocus"})
               dirty?                (assoc :class "is-warning")
               invalid?              (assoc :class "is-danger"))]
            (when invalid?
              [:span.icon.is-small.is-right [:i.fas.fa-warning]])]
           [:div {:on-click #(swap! *edit assoc :editing? true)}
            (case type
              (:text :email) value
              :markdown      [markdown value placeholder])])
         (when dirty?
           [:div.buttons.is-pulled-right {:style {:margin-top "5px"}}
            [:button.button.is-small.is-primary
             (if invalid?
               {:disabled "disabled"}
               {:title    (case type
                            (:text :email) "Press Enter"
                            :markdown      "Press Ctrl+Enter")
                :on-click #(commit-edit! *edit field edit-value)})
             "Save"]
            [:button.button.is-small.is-text
             {:title    "Press Escape"
              :on-click #(reset-edit-state! type *edit value)}
             "Cancel"]])]))))
