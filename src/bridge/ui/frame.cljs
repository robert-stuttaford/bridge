(ns bridge.ui.frame
  (:require [bridge.ui.base :as ui.base]
            [bridge.ui.component.modal :as ui.modal]
            [bridge.ui.routes :as ui.routes]
            [bridge.ui.util :refer [<== ==> log]]
            [reagent.core :as r]))

(defmethod ui.base/view :home [_]
  [:div "home"])

(defn add-toggle-menu-handler [{:keys [on-click] :as params} toggle-menu-fn]
  (assoc params :on-click (fn []
                            (on-click)
                            (toggle-menu-fn))))

(defn maybe-add-toggle-menu-handler [toggle-menu-fn menu-active? params]
  (cond-> params
    menu-active?
    (add-toggle-menu-handler toggle-menu-fn)))

(defn navbar []
  (let [*menu-active?  (r/atom false)
        toggle-menu-fn #(swap! *menu-active? not)]
    (fn []
      (let [menu-active?                  @*menu-active?
            maybe-add-toggle-menu-handler (partial maybe-add-toggle-menu-handler
                                                   toggle-menu-fn menu-active?)]
        [:nav.navbar.is-fixed-top.is-info
         [:div.container
          [:div.navbar-brand
           [:a.navbar-item (ui.routes/turbolink :home)
            [:img {:src (str "http://www.clojurebridge.org/assets/images/"
                             "logo-small.png")
                   :style {:background "white"
                           :max-height "32px"
                           :border-radius "16px"
                           :-mox-border-radius "16px"
                           :-webkit-border-radius "16px"}}]]
           [:a.navbar-burger (cond-> {:role          "button"
                                      :aria-label    "menu"
                                      :aria-expanded menu-active?
                                      :on-click      toggle-menu-fn}
                               menu-active? (assoc :class "is-active"))
            [:span {:aria-hidden "true"}]
            [:span {:aria-hidden "true"}]
            [:span {:aria-hidden "true"}]]]
          [:div.navbar-menu (when menu-active? {:class "is-active"})
           [:div.navbar-start
            [:a.navbar-item (-> (ui.routes/turbolink :list-events)
                                maybe-add-toggle-menu-handler) "All Events"]
            [:a.navbar-item (-> (ui.routes/turbolink :create-event)
                                maybe-add-toggle-menu-handler) "Create Event"]]
           [:div.navbar-end
            [:div.navbar-divider]
            [:a.navbar-item (-> (ui.routes/turbolink :edit-profile)
                                maybe-add-toggle-menu-handler)
             (:person/name (<== [:bridge.ui/active-person]))]
            [:a.navbar-item {:href "/logout"} "Sign Out"]]]]]))))

(defn error-modal []
  (when-some [error (<== [:bridge.ui/network-error])]
    [ui.modal/warning-modal {:is-active?-fn (constantly true)
                             :close!-fn     identity}
     [:div "There was a network error:"
      [:pre (pr-str error)]]]))

(defn app []
  [:div
   [navbar]
   [:div.container {:style {:margin-top "6rem"}}
    (ui.base/view (<== [:bridge.ui/current-view]))
    [error-modal]]])
