(require '[cljs.build.api :as api]
         '[clojure.string :as string]
         '[figwheel-sidecar.repl-api :as figwheel]
         '[figwheel-sidecar.components.nrepl-server :as figwheel.nrepl])

(def source-dir "src")

(def compiler-config
  {:main          'bridge.main
   :output-to     "resources/js/app.js"
   :source-map    "resources/js/app.js.map"
   :output-dir    "resources/js/out"
   :asset-path    "/js/out"
   :optimizations :advanced})

(def dev-config
  (merge compiler-config
         {:optimizations :none
          :source-map    true}))

(def nrepl-options
  {:nrepl-port       7890
   :nrepl-middleware ["cider.nrepl/cider-middleware"
                      "refactor-nrepl.middleware/wrap-refactor"
                      "cemerick.piggieback/wrap-cljs-repl"]})

(def ensure-nrepl-port! #(spit ".nrepl-port" (:nrepl-port nrepl-options)))

(def figwheel-options
  {:figwheel-options nrepl-options
   :all-builds       [{:id           "dev"
                       :figwheel     {:on-jsload "bridge.main/refresh"}
                       :source-paths [source-dir "dev"]
                       :compiler     dev-config}]})

;;; Tasks --------------------------------------------------------------------------------

(defmulti task first)

(defmethod task :default [_]
  (prn "Available tasks: "))

;; `clj -A:cljs:dev build.clj repl`
(defmethod task "repl" [_]
  (ensure-nrepl-port!)
  (figwheel.nrepl/start-nrepl-server nrepl-options nil)
  (println "Started nREPL server on port:" (:nrepl-port nrepl-options)))

;; `clj -A:cljs:dev build.clj figwheel`
(defmethod task "figwheel" [_]
  (ensure-nrepl-port!)
  (figwheel/start-figwheel! figwheel-options)
  (figwheel/cljs-repl))

;; `clj -A:cljs build.clj compile`
(defmethod task "compile" [_]
  (api/build source-dir compiler-config))

(task *command-line-args*)
