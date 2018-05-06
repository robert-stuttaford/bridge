(ns bridge.person.spec
  (:require bridge.spec
            [clojure.spec.alpha :as s]))

;;; account

(s/def :person/name :bridge.spec/required-string)
(s/def :person/email :bridge.spec/email)
(s/def :person/password :bridge.spec/required-string)
(s/def :person/status #{:status/active :status/suspended :status/deleted})
(s/def :person/confirm-email-token :bridge.spec/nonce)
(s/def :person/reset-password-token :bridge.spec/nonce)

(s/def :bridge/new-person
  (s/keys :req [:person/name :person/email :person/password]))

(s/def :bridge/person
  (s/merge :bridge/new-person (s/keys :req [:person/status])))

(s/def :bridge/new-person-tx
  (s/merge :bridge/person (s/keys :req [:person/confirm-email-token])))

;;; event registration

(s/def :person/minor? boolean?)
(s/def :person/spoken-languages :bridge.spec/optional-string)
(s/def :person/gender-identity :bridge.spec/optional-string)
(s/def :person/agree-to-code-of-conduct? boolean?)
(s/def :person/food-preferences :bridge.spec/optional-string)
(s/def :person/t-shirt-size :bridge.spec/optional-string)

;; attendees
(s/def :person/past-programming-experience :bridge.spec/optional-string)
(s/def :person/experience-with-language? boolean?)
(s/def :person/attended-event-before? boolean?)

;; coaches
(s/def :person/phone-number :bridge.spec/optional-string)
(s/def :person/coaching-languages :bridge.spec/optional-string)
(s/def :person/past-coaching-experience :bridge.spec/optional-string)
(s/def :person/background-experience :bridge.spec/optional-string)
(s/def :person/preferred-coachee-level :bridge.spec/optional-string)
(s/def :person/floating-coach? boolean?)

(def attr->field-config
  #:person{:name
           #:field {:title            "Name"
                    :type             :text
                    :edit-by-default? true}
           :food-preferences
           #:field {:title            "Food preferences"
                    :type             :text
                    :edit-by-default? true}
           :t-shirt-size
           #:field {:title            "T shirt size"
                    :type             :text
                    :edit-by-default? true}
           :gender-identity
           #:field {:title            "Gender identity"
                    :type             :text
                    :edit-by-default? true}
           :phone-number
           #:field {:title            "Phone number"
                    :type             :text
                    :edit-by-default? true}
           :spoken-languages
           #:field {:title            "Spoken languages"
                    :type             :text
                    :edit-by-default? true}
           :past-programming-experience
           #:field {:title            "Past programming experience"
                    :type             :multiline-text
                    :edit-by-default? true}
           :background-experience
           #:field {:title            "Background experience"
                    :type             :multiline-text
                    :edit-by-default? true}
           :coaching-languages
           #:field {:title            "Coaching languages"
                    :type             :text
                    :edit-by-default? true}
           :past-coaching-experience
           #:field {:title            "Past coaching experience"
                    :type             :multiline-text
                    :edit-by-default? true}
           :preferred-coachee-level
           #:field {:title            "Preferred coachee level"
                    :type             :text
                    :edit-by-default? true}
           :minor?
           #:field {:title "Are you a minor? (below the age of 18)"
                    :type  :checkbox}
           :floating-coach?
           #:field {:title "Are you willing to coach more than one group?"
                    :type  :checkbox}
           :experience-with-language?
           #:field {:title "Do you have any experience with Clojure?"
                    :type  :checkbox}
           :attended-event-before?
           #:field {:title "Have you been to ClojureBridge before?"
                    :type  :checkbox}
           :agree-to-code-of-conduct?
           #:field {:title "Do you agree to our Code of Conduct?"
                    :type  :checkbox}})

(defn attr->field [attr]
  (merge #:field{:edit-state-key :bridge.person.ui/profile-for-editing
                 :commit-action  :bridge.person.ui/update-field-value!
                 :attr           attr}
         (attr->field-config attr)))
