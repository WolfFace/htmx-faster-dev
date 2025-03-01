(ns htmx-faster.ui.layout
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [hiccup.util :as h-util]
    [hiccup2.core :as hiccup]
    [htmx-faster.ui.header :as header]
    [htmx-faster.ui.sidebar :as sidebar]))

(defn insert-hiccup
  [in to-insert]
  (if (every? coll? to-insert)
    (into in to-insert)
    (conj in to-insert)))

(defn mainbar
  [{:keys [content hide-sidebar?]}]
  (if hide-sidebar?
    (insert-hiccup [:main.min-h-screen.p-4.w-full] content)
    (insert-hiccup
      [:main#main-content {:class "min-h-[calc(100vh-113px)] flex-1 overflow-y-inherit p-4 pt-0 md:pl-64"}]
      content)))

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

(defn meta-tags
  [{:keys [name description slug og-image-kind]}]
  (let [image-url (format "%s/og/image/%s/%s"
                          (System/getenv "BASE_URL")
                          og-image-kind
                          slug)]
    (apply concat
      [[[:title (if (not (str/blank? name)) (str name " | HTMXFaster") "HTMXFaster")]
        [:meta {:name "description" :content description}]
        [:meta {:property "og:title" :content name}]
        [:meta {:property "og:description" :content description}]]
       (when og-image-kind
         [[:meta {:property "og:image:alt" :content (format "About the %s" og-image-kind)}]
          [:meta {:property "og:image:type" :content "image/png"}]
          [:meta {:property "og:image" :content image-url}]
          [:meta {:property "og:image:width" :content "1200"}]
          [:meta {:property "og:image:height" :content "630"}]
          [:meta {:name "twitter:card" :content "summary_large_image"}]
          [:meta {:name "twitter:title" :content name}]
          [:meta {:name "twitter:description" :content description}]
          [:meta {:name "twitter:image:alt" :content (format "About the %s" og-image-kind)}]
          [:meta {:name "twitter:image:type" :content "image/png"}]
          [:meta {:name "twitter:image" :content image-url}]
          [:meta {:name "twitter:image:width" :content "1200"}]
          [:meta {:name "twitter:image:height" :content "630"}]])])))

(defn layout
  [req {:keys [_content meta hide-sidebar?] :as params}]
  [:html
   {:class "h-full" :lang "en"}
   (into
     [:head
      [:meta {:charset "UTF-8"}]
      [:link {:rel "icon" :href "/favicon.svg" :type "image/svg+xml"}]
      [:style (h-util/raw-string (slurp (io/resource "static/main.css")))]
      [:script {:defer "true" :src "/htmx.js"}]
      [:script {:defer "true" :src "/preload.js"}]
      [:script {:defer "true" :src "/alpine.js"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]]
     (meta-tags meta))
   [:body.flex.flex-col.overflow-y-auto.overflow-x-hidden.antialiased
    {:hx-boost "true"
     :hx-ext "preload"}
    [:div
     (header/header req)
     [:div
      {:class "pt-[85px] sm:pt-[70px]"}
      (if hide-sidebar?
        (mainbar params)
        [:div.flex.flex-grow.font-mono
         (sidebar/sidebar)
         (mainbar params)])]]
    (footer)]])

(defn render-page
  [req params]
  {:status 200
   :body (str
           "<!DOCTYPE html>"
           \newline
           (hiccup/html (layout req params)))
   :headers {"Cache-Control" "max-age=10"
             "Content-Type"  "text/html;charset=utf-8"}})
