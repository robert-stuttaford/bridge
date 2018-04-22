(ns bridge.main
  (:require [bridge.ui :as ui]
            [bridge.ui.frame :as ui.frame]
            [bridge.ui.routes :as ui.routes]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(set! s/*explain-out* expound/printer)

(s/check-asserts true)

(defn ^:export refresh []
  (ui.routes/start-routing! ui/app-routes)
  (rf/dispatch-sync [:bridge.ui/initialize])
  (r/render [ui.frame/app] (js/document.getElementById "mount")))
