(ns bridge.ui.component.date
  (:require cljsjs.moment
            cljsjs.prop-types ; not in React 16.2+, but react-dates still uses it
            cljsjs.react-dates
            [reagent.core :as r]))

(def moment->date #(.toDate %))

(def date->moment js/moment)

(def date-range-picker
  (r/adapt-react-class js/ReactDates.DateRangePicker))

(defn select-dates [*form start-date-key initial-start-date
                    end-date-key initial-end-date]
  (let [*focused (r/atom {})]
    (fn []
      (let [focused    @*focused
            start-date (some-> (or (get @*form start-date-key) initial-start-date)
                               date->moment)
            end-date   (some-> (or (get @*form end-date-key) initial-end-date)
                               date->moment)]
        [date-range-picker
         (cond-> {:on-focus-change #(reset! *focused (js->clj %))
                  :on-dates-change
                  #(swap! *form
                          (fn [state]
                            (let [{:strs [startDate endDate]} (js->clj %)]
                              (merge state
                                     {start-date-key (some-> startDate moment->date)
                                      end-date-key   (some-> endDate moment->date)}))))}
           start-date (assoc :start-date start-date)
           end-date   (assoc :end-date end-date)
           (contains? #{"startDate" "endDate"} focused)
           (assoc :focused-input focused))]))))
