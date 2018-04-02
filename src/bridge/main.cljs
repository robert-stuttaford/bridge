(ns bridge.main
  (:require bridge.event.ui
            bridge.ui
            [bridge.ui.frame :as ui.frame]
            [bridge.ui.routes :as ui.routes]
            [cljs.reader :as edn]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(set! s/*explain-out* expound/printer)

(s/check-asserts true)

(rf/reg-event-db ::initialize
  (fn [_ _]
    (merge (some-> (js/document.getElementById "initial-state")
                   .-textContent
                   edn/read-string)
           bridge.ui/initial-state)))

(def routes
  ["/app"
   (merge {"" :home}
          bridge.event.ui/routes)])

(defn ^:export refresh []
  (ui.routes/start-routing! routes)
  (rf/dispatch-sync [::initialize])
  (r/render [ui.frame/app] (js/document.getElementById "mount")))
