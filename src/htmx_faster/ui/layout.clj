(ns htmx-faster.ui.layout
  (:require
    [clojure.java.io :as io]
    [hiccup.util :as h-util]
    [htmx-faster.ui.sidebar :as sidebar]))

(defn header
  []
  [:header
   {:class "fixed top-0 z-10 flex h-[90px] w-[100vw] flex-grow items-center justify-between border-b-2 border-accent2 bg-background p-2 pb-[4px] pt-2 sm:h-[70px] sm:flex-row sm:gap-4 sm:p-4 sm:pb-[4px] sm:pt-0"}
   [:div.flex.flex-grow.flex-col
    [:div.absolute.right-2.top-2.flex.justify-end.pt-2.font-sans.text-sm.hover:underline.sm:relative.sm:right-0.sm:top-0
     [:button.flex.flex-row.items-center.gap-1
      {:type "button" :aria-haspopup "dialog" :aria-expanded "false" :aria-controls "radix-:R2jb:" :data-state "closed"}
      "Log in"
      [:svg {:viewBox "0 0 10 6" :class "h-[6px] w-[10px]"} [:polygon {:points "0,0 5,6 10,0"}]]]]
    [:div.flex.w-full.flex-col.items-start.justify-center.sm:w-auto.sm:flex-row.sm:items-center.sm:gap-2
     [:a.text-4xl.font-bold.text-accent1 {:href "/"} "HTMXFaster"]
     [:div.items.flex.w-full.flex-row.items-center.justify-between.gap-4
      [:div.mx-0.flex-grow.sm:mx-auto.sm:flex-grow-0
       [:div.font-sans
        [:div.relative.flex-grow
         [:div.relative
          [:input.flex.h-9.w-full.border.border-gray-500.bg-transparent.px-3.py-1.text-sm.outline-none.pr-12.font-sans.font-medium
           {:type "text" :class "sm:w-[300px] md:w-[375px]" :autocapitalize "none" :autocorrect "off" :placeholder "Search..." :value ""}]
          [:svg.lucide.lucide-x.absolute.right-7.top-2.h-5.w-5.text-muted-foreground.hidden {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2" :stroke-linecap "round" :stroke-linejoin "round"} [:path {:d "M18 6 6 18"}] [:path {:d "m6 6 12 12"}]]]]]]
      [:div.flex.flex-row.justify-between.space-x-4
       [:div.relative [:a.text-lg.text-accent1.hover:underline {:href "/order"} "ORDER"]]
       [:a.hidden.text-lg.text-accent1.hover:underline.md:block {:href "/order-history"} "ORDER HISTORY"]
       [:a.block.text-lg.text-accent1.hover:underline.md:hidden {:aria-label "Order History" :href "/order-history"} [:svg.lucide.lucide-menu {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2" :stroke-linecap "round" :stroke-linejoin "round"} [:line {:x1 "4" :x2 "20" :y1 "12" :y2 "12"}] [:line {:x1 "4" :x2 "20" :y1 "6" :y2 "6"}] [:line {:x1 "4" :x2 "20" :y1 "18" :y2 "18"}]]]]]]]])

(defn mainbar
  [content]
  [:main#main-content
   {:class "min-h-[calc(100vh-113px)] flex-1 overflow-y-inherit p-4 pt-0 md:pl-64"}
   content])

(defn footer
  []
  [:footer {:class "fixed bottom-0 flex h-12 w-screen flex-col items-center justify-between space-y-2 border-t border-gray-400 bg-background px-4 font-sans text-[11px] sm:h-6 sm:flex-row sm:space-y-0"}
   [:div.flex.flex-wrap.justify-center.space-x-2.pt-2.sm:justify-start [:span.hover:bg-accent2.hover:underline "Home"] [:span "|"] [:span.hover:bg-accent2.hover:underline "FAQ"] [:span "|"] [:span.hover:bg-accent2.hover:underline "Returns"] [:span "|"] [:span.hover:bg-accent2.hover:underline "Careers"] [:span "|"] [:span.hover:bg-accent2.hover:underline "Contact"]] [:div.text-center.sm:text-right "By using this website, you agree to check out the" [:a.font-bold.text-accent1.hover:underline {:target "_blank" :href "https://github.com/ethanniser/NextFaster"} "Source Code"]]])

(defn notification
  []
  [:section {:aria-label "Notifications alt+T" :tabindex "-1"}
   [:ol {:dir "ltr" :tabindex "-1" :data-sonner-toaster "true" :data-theme "light" :data-y-position "bottom" :data-x-position "right" :style "--front-toast-height: 163.5px; --offset: 32px; --width: 356px; --gap: 14px;"}
    [:li {:aria-live "polite" :aria-atomic "true" :role "status" :tabindex "0" :class "" :data-sonner-toast "" :data-styled "true" :data-mounted "true" :data-promise "false" :data-removed "false" :data-visible "true" :data-y-position "bottom" :data-x-position "right" :data-index "0" :data-front "true" :data-swiping "false" :data-dismissible "true" :data-swipe-out "false" :data-expanded "false" :style "--index: 0; --toasts-before: 0; --z-index: 1; --offset: 0px; --initial-height: 163.5px;"}
     [:button {:aria-label "Close toast" :data-disabled "false" :data-close-button "true" :class ""}
      [:svg {:xmlns "http://www.w3.org/2000/svg" :width "12" :height "12" :viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "1.5" :stroke-linecap "round" :stroke-linejoin "round"} [:line {:x1 "18" :y1 "6" :x2 "6" :y2 "18"}] [:line {:x1 "6" :y1 "6" :x2 "18" :y2 "18"}]]]
     [:div {:data-content "" :class ""}
      [:div {:data-title "" :class ""} "🚀 Welcome to NextFaster!"]
      [:div {:data-description "" :class ""}
       "This is a highly performant e-commerce template using Next.js. All of the 1M products on this site are AI generated."
       [:hr.my-2]
       "This demo is to highlight the speed a full-stack Next.js site can achieve."
       [:a.font-semibold.text-accent1.hover:underline {:href "https://github.com/ethanniser/NextFaster" :target "_blank"} "Get the Source"] "."]]]]])

(defn layout
  [content]
  [:html
   {:class "h-full" :lang "en"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "HTMXFaster"]
    [:link {:rel "icon" :href "/favicon.svg" :type "image/svg+xml"}]
    ;[:link {:rel "stylesheet" :type "text/css" :href "/main.css"}]

    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin "true"}]
    [:link {:href "https://fonts.googleapis.com/css2?family=Geist+Mono:wght@100..900&family=Geist:wght@100..900&display=swap" :rel "stylesheet"}]


    ;[:link {:rel "preload" :href "/fonts/66f30814ff6d7cdf.p.woff2" :as "font" :type "font/woff2" :crossorigin "true"}]
    ;[:link {:rel "preload" :href "/fonts/e11418ac562b8ac1-s.p.woff2" :as "font" :type "font/woff2" :crossorigin "true"}]
    ;[:link {:rel "stylesheet" :href "/main.css"}]
    ;[:style (slurp (io/resource "static/main.css"))]
    ;[:script {:src "https://unpkg.com/htmx.org@2.0.4" :integrity "sha384-HGfztofotfshcF7+8n44JQL2oJmowVChPTg48S+jvZoztPfvwD79OC/LTtG6dMp+" :crossorigin "anonymous"}]
    ;[:script {:src "https://unpkg.com/htmx-ext-preload@2.1.0/preload.js"}]
    ;[:script {:src "/htmx.js"}]
    ;[:script {:src "/preload.js"}]

    ;[:link {:rel "stylesheet" :type "text/css" :href "/main.css"}]
    [:style (h-util/raw-string (slurp (io/resource "static/main.css")))]
    [:script (h-util/raw-string (slurp (io/resource "static/htmx.js")))]
    [:script (h-util/raw-string (slurp (io/resource "static/preload.js")))]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]]
   [:body.flex.flex-col.overflow-y-auto.overflow-x-hidden.antialiased
    {:hx-boost "true"
     :hx-ext "preload"}
    [:div
     (header)
     [:div
      {:class "pt-[85px] sm:pt-[70px]"}
      [:div.flex.flex-grow.font-mono
       (sidebar/sidebar)
       (mainbar content)]]]
    (footer)]])


;[:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
;[:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin "true"}]
;[:link {:href "https://fonts.googleapis.com/css2?family=Geist+Mono:wght@100..900&family=Geist:wght@100..900&display=swap" :rel "stylesheet"}]
