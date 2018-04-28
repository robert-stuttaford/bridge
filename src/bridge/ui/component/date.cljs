(ns bridge.ui.component.date
  (:require cljsjs.moment
            cljsjs.prop-types ; not in React 16.2+, but react-dates still uses it
            cljsjs.react-dates
            [reagent.core :as r]))

(def moment->date #(.toDate %))

(def date->moment js/moment)

(def day-picker
  (r/adapt-react-class js/ReactDates.SingleDatePicker))

(defn select-date [date-fn on-change]
  (let [*focused (r/atom nil)]
    (fn []
      (let [date (some-> (date-fn) date->moment)]
        [day-picker
         (cond-> {:id              "day-picker"
                  :focused         (boolean @*focused)
                  :on-focus-change #(reset! *focused (get (js->clj %) "focused"))
                  :on-date-change  #(on-change (moment->date %))}
           date (assoc :date date))]))))

(def date-range-picker
  (r/adapt-react-class js/ReactDates.DateRangePicker))

(defn select-dates [start-date-fn end-date-fn on-change]
  (let [*focused (r/atom {})]
    (fn []
      (let [focused    @*focused
            start-date (some-> (start-date-fn) date->moment)
            end-date   (some-> (end-date-fn) date->moment)]
        [date-range-picker
         (cond-> {:on-focus-change #(reset! *focused (js->clj %))
                  :on-dates-change
                  #(let [{:strs [startDate endDate]} (js->clj %)]
                     (on-change {:start (some-> startDate moment->date)
                                 :end   (some-> endDate moment->date)}))}
           start-date (assoc :start-date start-date)
           end-date   (assoc :end-date end-date)
           (contains? #{"startDate" "endDate"} focused)
           (assoc :focused-input focused))]))))

(defn select-dates-for-form [*form start-date-key initial-start-date
                             end-date-key initial-end-date]
  [select-dates
   #(or (get @*form start-date-key) initial-start-date)
   #(or (get @*form end-date-key) initial-end-date)
   #(swap! *form
           (fn [state]
             (let [{:keys [start end]} %]
               (merge state
                      {start-date-key start
                       end-date-key   end}))))])
