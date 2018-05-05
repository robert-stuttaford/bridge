(ns bridge.person.ui.edit-profile
  (:require [bridge.person.spec :as event.spec]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==> log]]))

;; TODO
;; :person/email - request fresh confirmation email
;; :person/password - with confirmation field

;; :person/minor?
;; :person/floating-coach?
;; :person/experience-with-target-language?
;; :person/attended-event-before?
;; :person/agree-to-code-of-conduct?

(defn edit-profile []
  [:div

   [:div.level
    [:div.level-left
     [:div.level-item
      [:h3.title.is-4.spaced "Edit profile"]]]]

   [:div.is-divider]

   [:div.columns
    [:div.column.is-two-fifths
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/name)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/spoken-languages)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/food-preferences)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/t-shirt-size)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/gender-identity)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/phone-number)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/past-programming-experience)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/background-experience)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/coaching-languages)]
     [ui.edit-field/edit-text-field
      (event.spec/attr->field :person/preferred-coachee-level)]]]])

