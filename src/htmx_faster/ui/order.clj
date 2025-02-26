(ns htmx-faster.ui.order
  (:require
    [hiccup2.core :as hiccup]
    [htmx-faster.img :as img]
    [htmx-faster.ui.cart :as cart]
    [htmx-faster.ui.layout :as layout]
    [htmx-faster.ui.login :as login]))

(defn remove-svg
  []
  [:svg.lucide.lucide-x.h-6.w-6
   {:xmlns "http://www.w3.org/2000/svg"
    :width "24" :height "24"
    :viewBox "0 0 24 24"
    :fill "none"
    :stroke "currentColor"
    :stroke-width "2"
    :stroke-linecap "round"
    :stroke-linejoin "round"}
   [:path {:d "M18 6 6 18"}]
   [:path {:d "m6 6 12 12"}]])

(defn order
  [req]
  (let [cart (cart/decode-cart-cookie req)
        products (cart/get-cart-products cart)
        signed-in? (not (login/get-request-jwt-token-sub req))]
    [:div.container.mx-auto.p-1.sm:p-3
     [:div.flex.items-center.justify-between.border-b.border-gray-200
      [:h1.text-2xl.text-accent1 "Order"]]
     [:div.flex.grid-cols-3.flex-col.gap-8.pt-4.lg:grid
      [:div.col-span-2
       [:div.pb-4
        [:p.font-semibold.text-accent1 "Delivers in 2-4 weeks"]
        [:p.text-sm.text-gray-500 "Need this sooner?"]]
       (when-not (seq products) [:div#orders-cart.flex.flex-col.space-y-10 [:p "No items in cart"]])
       (into [:div#orders-cart.flex.flex-col.space-y-10]
            (map (fn [product]
                   [:div.product-item.flex.flex-row.items-center.justify-between.border-t.border-gray-200.pt-4
                    [:a
                     {:href (format "/products/%s/%s/%s"
                                    (:category_slug product)
                                    (:subcategory_slug product)
                                    (:slug product))}
                     [:div.flex.flex-row.space-x-2
                      [:div.flex.h-24.w-24.items-center.justify-center.bg-gray-100
                       [:img {:alt (format "Product %s" (:name product))
                              :loading "lazy"
                              :width "256"
                              :height "256"
                              :decoding "async"
                              :style "color: transparent;"
                              :src (img/local-image-url (:image_url product) 81)}]]
                      [:div.flex-grow.sm:max-w-full
                       {:class "max-w-[100px]"}
                       [:h2.font-semibold (:name product)]
                       [:p.text-sm.md:text-base (:description product)]]]]
                    [:div.flex.items-center.justify-center.md:space-x-10
                     [:div.flex.flex-col-reverse.md:flex-row.md:gap-4
                      [:p (get cart (:slug product))]
                      [:div.flex.md:block
                       [:div.min-w-8.text-sm.md:min-w-24.md:text-base
                        [:p (format "%s each" (format "$%,.2f" (:price product)))]]]
                      [:div.min-w-24
                       [:p.font-semibold (format "$%,.2f" (* (get cart (:slug product)) (:price product)))]]]
                     [:form
                      {:hx-post "/order/remove"
                       :hx-trigger "submit"
                       :hx-swap "delete"
                       :hx-target "closest .product-item"}
                      [:button
                       {:type "submit"}
                       [:input {:type "hidden" :value (:slug product) :name "product-slug"}]
                       (remove-svg)]]]]))
            products)]
      [:div.space-y-4
       [:div.rounded.bg-gray-100.p-4
        (cart/cart-total-price cart products)
        [:p.text-sm.text-gray-500 "Applicable shipping and tax will be added."]]
       (when signed-in? [:p.font-semibold.text-accent1 "Log in to place an order"])
       (when signed-in? [:div.flex.flex-col.space-y-6 (login/login-form)])]]]))

(defn remove-item-handler
  [req]
  (let [product-slug (get-in req [:params "product-slug"])
        cart (cart/remove-product-from-cart (cart/decode-cart-cookie req) product-slug)
        products (cart/get-cart-products cart)]
    {:status 200
     :body (str (hiccup/html (cart/cart-total-price cart products true))
                "\n"
                (hiccup/html (cart/amount-badge-html cart true))
                "\n"
                ;; When no products in cart left
                (when-not (seq cart)
                  (hiccup/html [:div#orders-cart.flex.flex-col.space-y-10
                                {:hx-swap-oob "true"}
                                [:p "No items in cart"]])))
     :headers {"Set-Cookie" (cart/set-cart-cookie-header cart)
               "Content-Type" "text/html"}}))

(defn page
  [req]
  (layout/render-page
    req
    {:hide-sidebar? true
     :content (order req)}))
