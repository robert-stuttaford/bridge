(ns bridge.ui.component.edit-field
  (:require bridge.data.edit.spec
            [bridge.data.string :as data.string]
            [bridge.ui.spec :as ui.spec]
            [bridge.ui.util :refer [<== ==> log]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))

(defn init-edit-state [edit-by-default? value]
  {:orig-value value
   :edit-value value
   :editing?   edit-by-default?})

(defn prepare-edit-attrs [attrs attr->field-config]
  (reduce (fn [attrs [attr {:field/keys [edit-by-default?]}]]
            (assoc attrs attr (init-edit-state edit-by-default? (get attrs attr))))
          attrs
          attr->field-config))

(defn reset-edit-state [db [_ {:field/keys [edit-state-key attr edit-by-default?]}]]
  (update-in db [edit-state-key attr]
             #(init-edit-state edit-by-default? (:orig-value %))))

(rf/reg-event-db ::reset-edit-state [ui.spec/check-spec-interceptor] reset-edit-state)

(rf/reg-event-db ::start-editing
  [ui.spec/check-spec-interceptor]
  (fn [db [_ {:field/keys [edit-state-key attr]}]]
    (assoc-in db [edit-state-key attr :editing?] true)))

(rf/reg-event-db ::update-edit-state
  [ui.spec/check-spec-interceptor]
  (fn [db [_ {:field/keys [edit-state-key attr]} edit-value]]
    (update-in db [edit-state-key attr]
               (fn [{:keys [orig-value] :as edit-state}]
                 (merge edit-state
                        {:edit-value edit-value
                         :dirty?     (not= orig-value edit-value)
                         :invalid?   (if-some [spec (s/get-spec attr)]
                                       (not (s/valid? spec edit-value))
                                       false)})))))

(rf/reg-event-fx ::commit-edit
  [ui.spec/check-spec-interceptor]
  (fn [{:keys [db]} [_ {:field/keys [edit-state-key attr commit-action] :as field}
                    edit-value]]
    {:db       (assoc-in db [edit-state-key attr :busy?] true)
     :dispatch [commit-action field edit-value]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn edit-text-field [{:field/keys [type edit-state-key attr title placeholder]
                        :as field}]
  (let [{:keys [orig-value edit-value error editing? dirty? invalid? busy?]}
        (get (<== [edit-state-key]) attr)
        edit!   #(==> [::start-editing field])
        update! #(==> [::update-edit-state field %])
        commit! #(==> [::commit-edit field edit-value])
        reset!  #(==> [::reset-edit-state field])]
    [:div.field
     [:label.label [:u title]
      (when-not editing? " (click to edit)")
      (when (some? error)
        (case error
          :bridge.error/uniqueness-conflict
          [:span.has-text-danger {:style {:margin-left "1rem"}}
           (str "That " (str/lower-case title) " is already in use.")]
          [:code (name error)]))]
     (if editing?
       [:div.control
        (when (or invalid? busy?) {:class "has-icons-right"})
        [(case type
           (:text :email)              :input.input
           (:multiline-text :markdown) :textarea.textarea)
         (cond-> {:placeholder (str (or placeholder title)
                                    (when invalid? " is required!"))
                  :value       edit-value
                  :on-change   #(update! (.. % -currentTarget -value))
                  :on-key-up   (fn [e]
                                 (.stopImmediatePropagation (aget e "nativeEvent"))
                                 (condp = (.-key e)
                                   "Enter"
                                   (when (and (not invalid?)
                                              (or (not= type :markdown)
                                                  (.-ctrlKey e)))
                                     (.preventDefault e)
                                     (commit!))

                                   "Escape"
                                   (reset!)
                                   nil))}
           (not (contains? #{:multiline-text :markdown} type)) (assoc :type (name type))
           (= type :markdown)       (merge {:rows      10
                                            :autoFocus "autofocus"})
           (= type :multiline-text) (assoc :rows 4)
           dirty?   (assoc :class "is-warning")
           invalid? (assoc :class "is-danger")
           busy?    (assoc :disabled "disabled"))]
        (cond invalid?
              [:span.icon.is-small.is-right [:i.fas.fa-exclamation-triangle]]
              busy?
              [:span.icon.is-small.is-right [:i.fas.fa-circle-notch.fa-spin]])]
       [:div {:on-click #(edit!)}
        (case type
          (:text :email :multiline-text) orig-value
          :markdown [markdown orig-value placeholder])])
     (when dirty?
       [:div.buttons.is-pulled-right {:style {:margin-top "5px"}}
        [:button.button.is-small.is-primary
         (if invalid?
           {:disabled "disabled"}
           {:title    (case type
                        (:text :email) "Press Enter"
                        :markdown      "Press Ctrl+Enter")
            :on-click #(commit!)})
         "Save"]
        [:button.button.is-small.is-text
         {:title    "Press Escape"
          :on-click #(reset!)}
         "Cancel"]])]))

(defn edit-checkbox [{:field/keys [edit-state-key attr title] :as field}]
  (let [{:keys [edit-value busy?]} (get (<== [edit-state-key]) attr)
        commit! #(==> [::commit-edit field %])]
    [:div.field
     [:div.control
      [:label.checkbox
       (if busy?
         [:span.icon.is-small.is-right [:i.fas.fa-circle-notch.fa-spin]]
         [:input.checkbox
          {:type      "checkbox"
           :value     "true"
           :checked   (true? edit-value)
           :on-change #(commit! (.. % -currentTarget -checked))}])
       " " title]]]))

(defn edit [{:field/keys [type] :as field}]
  (ui.spec/check-spec-error :bridge/edit-field-config field)
  (case type
    (:text :email :multiline-text :markdown)
    (edit-text-field field)

    :checkbox
    (edit-checkbox field)))
