(require '[figwheel-sidecar.repl-api :as figwheel])

(def figwheel-options
  {:figwheel-options
   {:nrepl-port       7890
    :nrepl-middleware ["cider.nrepl/cider-middleware"
                       "refactor-nrepl.middleware/wrap-refactor"]}

   :all-builds
   [{:id           "dev"
     :figwheel     {:on-jsload "bridge.main/refresh"}
     :source-paths ["src"]
     :compiler     {:main          'bridge.main
                    :output-to     "resources/js/app.js"
                    :output-dir    "resources/js/out"
                    :asset-path    "/js/out"
                    :optimizations :none
                    :source-map    true}}]})

(spit ".nrepl-port" (get-in figwheel-options [:figwheel-options :nrepl-port]))

(figwheel/start-figwheel! figwheel-options)
