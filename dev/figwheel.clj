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
     :compiler
     {:main            'bridge.main
      :output-to       "resources/js/app.js"
      :output-dir      "resources/js/out"
      :asset-path      "/js/out"
      :optimizations   :none
      :source-map      true
      ;; :closure-defines {"re_frame.trace.trace_enabled_QMARK_"        true
      ;;                   "day8.re_frame.tracing.trace_enabled_QMARK_" true}
      :preloads        ['devtools.preload
                        're-frisk.preload
                        ;; 'day8.re-frame-10x.preload
                        ]}}]})

(spit ".nrepl-port" (get-in figwheel-options [:figwheel-options :nrepl-port]))

(figwheel/start-figwheel! figwheel-options)
