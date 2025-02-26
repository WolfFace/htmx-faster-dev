(ns htmx-faster.ui.order-history
  (:require
    [htmx-faster.ui.layout :as layout]))

(defn page
  [req]
  (layout/render-page
    req
    {:hide-sidebar? true
     :content
     [[:h1.w-full.border-b-2.border-accent1.text-left.text-2xl.text-accent1 "Order History"]
      [:div.mx-auto.flex.max-w-md.flex-col.gap-4.text-black
       [:div.border-t.border-gray-200.pt-4
        [:table.w-full
         [:thead
          [:tr.text-left.text-sm.font-medium.text-gray-500
           [:th.pb-2 {:class "w-1/2"} "Product"]
           [:th.pb-2 {:class "w-1/4"} "Last Order Date"]
           [:th.pb-2 {:class "w-1/4"} "Purchase Order"]]]
         [:tbody
          [:tr
           [:td.py-8.text-center.text-gray-500
            {:colspan "3"}
            "You have no previous orders."
            [:br]
            "When you place an order, it will appear here."]]]]]]]}))
