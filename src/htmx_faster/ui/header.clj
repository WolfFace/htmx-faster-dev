(ns htmx-faster.ui.header
  (:require
    [htmx-faster.ui.cart :as cart]
    [htmx-faster.ui.login :as login]
    [htmx-faster.ui.search :as search]))

(defn menu-svg
  []
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :width "24" :height "24" :viewBox "0 0 24 24" :fill "none"
         :stroke "currentColor" :stroke-width "2" :stroke-linecap "round" :stroke-linejoin "round"}
   [:line {:x1 "4" :x2 "20" :y1 "12" :y2 "12"}]
   [:line {:x1 "4" :x2 "20" :y1 "6" :y2 "6"}]
   [:line {:x1 "4" :x2 "20" :y1 "18" :y2 "18"}]])

(defn login-button
  [req]
  [:div
   {:x-data "{open: false}"
    :x-on:click "open = true"
    :x-on:click.outside "open = false"}
   [:button.flex.flex-row.items-center.gap-1
    {:type "button"
     :x-on:click.stop "open = !open"
     :aria-haspopup "dialog"
     :aria-expanded "false"
     :aria-controls "radix-:R2jb:"}
    (if-let [username (login/get-request-jwt-token-sub req)]
      username
      "Log in")
    [:svg {:viewBox "0 0 10 6" :class "h-[6px] w-[10px]"} [:polygon {:points "0,0 5,6 10,0"}]]]
   [:template
    {:x-if "open"}
    (login/popup req)]])

(defn header
  [req]
  [:header
   {:class "fixed top-0 z-10 flex h-[90px] w-[100vw] flex-grow items-center justify-between border-b-2 border-accent2 bg-background p-2 pb-[4px] pt-2 sm:h-[70px] sm:flex-row sm:gap-4 sm:p-4 sm:pb-[4px] sm:pt-0"}
   [:div.flex.flex-grow.flex-col
    [:div.absolute.right-2.top-2.flex.justify-end.pt-2.font-sans.text-sm.hover:underline.sm:relative.sm:right-0.sm:top-0
     (login-button req)]
    [:div.flex.w-full.flex-col.items-start.justify-center.sm:w-auto.sm:flex-row.sm:items-center.sm:gap-2
     [:a.text-4xl.font-bold.text-accent1 {:href "/" :preload "mouseover" :preload-images "true" :hx-trigger "mousedown"} "HTMXFaster"]
     [:div.items.flex.w-full.flex-row.items-center.justify-between.gap-4
      (search/alpine-input)
      [:div.flex.flex-row.justify-between.space-x-4
       [:div.relative
        [:a.text-lg.text-accent1.hover:underline {:href "/order"} "ORDER"]
        (cart/amount-badge req)]
       [:a.hidden.text-lg.text-accent1.hover:underline.md:block {:href "/order-history"} "ORDER HISTORY"]
       [:a.block.text-lg.text-accent1.hover:underline.md:hidden {:aria-label "Order History" :href "/order-history"}
        (menu-svg)]]]]]])
