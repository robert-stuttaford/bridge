(ns bridge.ui.component.markdown
  (:require [bridge.data.string :as data.string]))

(defn markdown [content]
  [:div.content
   {:style {:padding          "1.5rem"
            :background-color "#eee"
            :border-radius    "0.8rem"
            :min-height       "330px"}
    :dangerouslySetInnerHTML
    {:__html (or (-> content
                     data.string/markdown->html
                     data.string/not-blank)
                 "(empty)")}}])
