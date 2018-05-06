(ns bridge.person.ui.edit-profile
  (:require [bridge.person.spec :as event.spec]
            [bridge.ui.component.edit-field :as ui.edit-field]
            [bridge.ui.util :refer [<== ==> log]]))

;; TODO
;; :person/email - request fresh confirmation email
;; :person/password - with confirmation field

(defn edit-profile []
  [:div

   [:div.level
    [:div.level-left
     [:div.level-item
      [:h3.title.is-4.spaced "Edit profile"]]]]

   [:div.is-divider]

   [:div.columns
    [:div.column.is-two-fifths
     [:h3.subtitle "For everyone"]

     [ui.edit-field/edit (event.spec/attr->field :person/name)]
     [ui.edit-field/edit (event.spec/attr->field :person/minor?)]
     [ui.edit-field/edit (event.spec/attr->field :person/spoken-languages)]
     [ui.edit-field/edit (event.spec/attr->field :person/gender-identity)]
     [ui.edit-field/edit (event.spec/attr->field :person/food-preferences)]
     [ui.edit-field/edit (event.spec/attr->field :person/t-shirt-size)]
     [ui.edit-field/edit (event.spec/attr->field :person/agree-to-code-of-conduct?)]

     [:div.is-divider]

     [:h3.subtitle "For attendees"]

     [ui.edit-field/edit (event.spec/attr->field :person/past-programming-experience)]
     [ui.edit-field/edit (event.spec/attr->field :person/experience-with-language?)]
     [ui.edit-field/edit (event.spec/attr->field :person/attended-event-before?)]]

    [:div.is-divider-vertical]

    [:div.column.is-two-fifths
     [:h3.subtitle "For coaches"]

     [ui.edit-field/edit (event.spec/attr->field :person/floating-coach?)]
     [ui.edit-field/edit (event.spec/attr->field :person/coaching-languages)]
     [ui.edit-field/edit (event.spec/attr->field :person/phone-number)]
     [ui.edit-field/edit (event.spec/attr->field :person/past-coaching-experience)]
     [ui.edit-field/edit (event.spec/attr->field :person/background-experience)]
     [ui.edit-field/edit (event.spec/attr->field :person/preferred-coachee-level)]]]])

