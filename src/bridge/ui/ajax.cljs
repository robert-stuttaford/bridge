(ns bridge.ui.ajax
  (:require ajax.edn
            day8.re-frame.http-fx
            [re-frame.core :as rf]))

(rf/reg-event-fx ::http-post
  (fn [db [_ data on-success]]
    {:http-xhrio
     (cond-> {:method          :post
              :uri             (str "/client/api?" (name (:action data)))
              :params          data
              :timeout         5000
              :format          (ajax.edn/edn-request-format)
              :response-format (ajax.edn/edn-response-format)
              :on-failure      [::http-post-failure]}
       on-success (assoc :on-success on-success))}))

(rf/reg-event-fx ::http-post-failure
  (fn [{:keys [db]} [_ result]]
    (prn ::http-post-failure result)
    {:db (assoc db :bridge.ui/network-error result)}))

(defn action [action params on-success]
  [::http-post (assoc params :action action) on-success])
