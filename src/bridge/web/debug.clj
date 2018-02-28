(ns bridge.web.debug
  (:require [bridge.web.template :as web.template]
            [buddy.auth :as buddy]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [bridge.config :as config]))

(defn pprint-str [val]
  (binding [clojure.pprint/*print-right-margin* 120]
    (with-out-str (pprint/pprint val))))

(defn debug-pre [val]
  [:pre {:style {:white-space "pre-nowrap"
                 :font-family "Fira Code"}}
   (pprint-str val)])

(defn system [req]
  (if-not (buddy/authenticated? req)
    (buddy/throw-unauthorized)
    (web.template/hiccup-response
     [:section.section
      [:h1.title "System"]
      (debug-pre (config/system))
      [:hr]
      [:h1.title "Request"]
      (debug-pre (->> (-> req
                          (dissoc :body :cookies :session/key)
                          (update :headers dissoc "cookie"))
                      (reduce-kv (fn [m k v]
                                   (cond-> m
                                     (some? v) (assoc k v))) {})
                      (into (sorted-map))))])))

(def routes
  {:routes
   '{"/system" [:system]}
   :handlers
   {:system #'system}})

