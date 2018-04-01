(ns bridge.main
  (:require bridge.ui
            bridge.ui.ajax
            bridge.event.ui
            [bridge.ui.routes :as ui.routes]
            [cljs.reader :as edn]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db ::initialize
  (fn [_ _]
    (merge (some-> (js/document.getElementById "initial-data")
                   .-textContent
                   edn/read-string)
           ui.routes/initial-state)))

(defn ^:export refresh []
  (rf/dispatch-sync [::initialize])
  (ui.routes/app-routes)
  (reagent/render [bridge.ui/app] (js/document.getElementById "mount")))
