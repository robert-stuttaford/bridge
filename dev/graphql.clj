(ns graphql
  (:require [clojure.edn :as edn]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.data.json :as json]
            [com.walmartlabs.lacinia.util :as lu]
            [com.walmartlabs.lacinia.schema :as schema]))

(defn get-hero [context arguments value]
  (let [{:keys [episode]} arguments]
    (if (= episode :NEWHOPE)
      {:id 1000
       :name "Luke"
       :home_planet "Tatooine"
       :appears_in ["NEWHOPE" "EMPIRE" "JEDI"]}
      {:id 2000
       :name "Lando Calrissian"
       :home_planet "Socorro"
       :appears_in ["EMPIRE" "JEDI"]})))

(declare star-wars-schema-raw)

(defn star-wars-schema []
  (-> star-wars-schema-raw
      (lu/attach-resolvers {:get-hero get-hero
                            :get-droid (constantly {})})
      schema/compile))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (let [query (get-in request [:query-params :query])
               result (lacinia/execute (star-wars-schema) query nil nil)]
           (json/write-str result))})


(def star-wars-schema-raw
  '{:enums
    {:episode
     {:description "The episodes of the original Star Wars trilogy."
      :values [:NEWHOPE :EMPIRE :JEDI]}}

    :interfaces
    {:character
     {:fields {:id {:type String}
               :name {:type String}
               :appears_in {:type (list :episode)}
               :friends {:type (list :character)}}}}

    :objects
    {:droid
     {:implements [:character]
      :fields {:id {:type String}
               :name {:type String}
               :appears_in {:type (list :episode)}
               :friends {:type (list :character)
                         :resolve :friends}
               :primary_function {:type (list String)}}}

     :human
     {:implements [:character]
      :fields {:id {:type String}
               :name {:type String}
               :appears_in {:type (list :episode)}
               :friends {:type (list :character)
                         :resolve :friends}
               :home_planet {:type String}}}}

    :queries
    {:hero {:type (non-null :character)
            :args {:episode {:type :episode}}
            :resolve :hero}

     :human {:type (non-null :human)
             :args {:id {:type String
                         :default-value "1001"}}
             :resolve :human}

     :droid {:type :droid
             :args {:id {:type String
                         :default-value "2001"}}
             :resolve :droid}}})
