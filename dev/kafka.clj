(ns kafka
  (:require [gregor.core :as gregor]
            [cheshire.core :as json]
            [clojure.pprint :as pprint]))

;; mocks

(defn evaluate-rules [& args]
  {:status :success})

(defn send-to-redis! [& args]
  {:status :success})

;; messages

(defn process-message [consumer producer [{:keys [key value topic] :as message}]]
  (let [payload       (try
                        (json/decode value keyword)
                        (catch Exception e
                          (:value message)))

        rule-output   (try
                        (evaluate-rules (get-in payload [:meta :resourceType])
                                        key payload)
                        (catch Exception e
                          (println "Error Executing Rules Evaluation: "
                                   (.getMessage e))))

        send-to-kafka (when (some? rule-output)
                        (gregor/send producer (str topic "_FACTS")
                                     (json/encode rule-output)))

        send-to-redis (when (some? send-to-kafka)
                        (send-to-redis! key rule-output))]

    (println "Message Payload => " (pprint/pprint payload))
    (println "Rule Evaluation Output => " (pprint/pprint rule-output))
    (println "Produced Message => " send-to-kafka)
    (println "Saved TO Redis => " send-to-redis)

    (gregor/commit-offsets! consumer)))

;; process

(def *processing?
  (atom true))

(defn init [{:keys [hosts group topics offset-reset auto-commit]}]
  (let [consumer (gregor/consumer hosts group topics
                                  {"auto.offset.reset"  offset-reset
                                   "enable.auto.commit" auto-commit})
        producer (gregor/producer hosts)]
    (while @*processing?
      (let [message (gregor/poll consumer)]
        (when-not (nil? message)
          (process-message consumer producer message))))))

(def kafka-config
  {:hosts        "localhost"
   :group        "test-group"
   :topics       ["RULES_PATIENT"]
   :offset-reset "latest"
   :auto-commit  "false"})

(comment
  (init kafka-config))
