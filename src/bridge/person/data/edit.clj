(ns bridge.person.data.edit
  (:require [bridge.data.datomic :as datomic]
            [clojure.string :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Edit

(def profile-for-editing-pull-spec
  [:person/name
   :person/minor?
   :person/spoken-languages
   :person/gender-identity
   :person/agree-to-code-of-conduct?
   :person/food-preferences
   :person/t-shirt-size
   :person/past-programming-experience
   :person/experience-with-target-language?
   :person/attended-event-before?
   :person/phone-number
   :person/coaching-languages
   :person/experience-with-target-language
   :person/background-experience
   :person/preferred-coachee-level
   :person/floating-coach?])

(defn profile-for-editing [db person-id]
  (datomic/pull db profile-for-editing-pull-spec person-id))

(def edit-whitelist
  #{:person/name
    :person/minor?
    :person/spoken-languages
    :person/gender-identity
    :person/agree-to-code-of-conduct?
    :person/food-preferences
    :person/t-shirt-size
    :person/past-programming-experience
    :person/experience-with-target-language?
    :person/attended-event-before?
    :person/phone-number
    :person/coaching-languages
    :person/experience-with-target-language
    :person/background-experience
    :person/preferred-coachee-level
    :person/floating-coach?})

(into {} (for [f edit-whitelist]
           [f
            #:field{:title            (str/replace (str/capitalize (name f))
                                                   "-" " ")
                    :type             :text
                    :edit-by-default? true}]))
