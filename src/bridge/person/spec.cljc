(ns bridge.person.spec
  (:require bridge.spec
            [clojure.spec.alpha :as s]))

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
