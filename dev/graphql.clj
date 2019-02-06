(ns graphql
  (:require [clojure.edn :as edn]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.data.json :as json]
            [com.walmartlabs.lacinia.util :as lu]
            [com.walmartlabs.lacinia.schema :as schema]))

(defn get-hero [context arguments value]
  (let [{:keys [episode]} arguments]
    {:id          1001
     :name        "Luke"
     :home_planet "Tatooine"
     :appears_in  ["NEWHOPE" "EMPIRE" "JEDI"]}))

(declare star-wars-schema-raw)

(defn star-wars-schema []
  (-> "star-wars.edn"
      slurp
      edn/read-string
      (lu/attach-resolvers {:hero get-hero
                            :droid (constantly {})
                            :friends (constantly {})
                            :human (constantly {})})
      schema/compile))

#_(star-wars-schema)

(defn handler [request]
  (let [query  (get-in request [:query-params :query])
        result (lacinia/execute (star-wars-schema) query nil nil)
        json   (json/write-str result)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    json}))


(handler {:query-params {:query "query { human(id: \"1001\") { name }}"}})
