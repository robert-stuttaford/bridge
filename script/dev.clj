
;; nREPL

(require '[clojure.tools.nrepl.server :refer [start-server]]
         '[refactor-nrepl.middleware :refer [wrap-refactor]])

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(def port 7890)

(spit ".nrepl-port" port)

(start-server :port port :handler (wrap-refactor (nrepl-handler)))
(println "Started nREPL on port" port)

(.addShutdownHook (Runtime/getRuntime)
                  (Thread. #(clojure.java.io/delete-file ".nrepl-port")))


;; Figwheel

(require '[figwheel.main :refer [start]])

(start "dev")
