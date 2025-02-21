(ns htmx-faster.ui.product
  (:require
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.layout :as layout]
    [htmx-faster.ui.products :as products]
    [pg.core :as pg]))

(defn format-price
  [amount]
  (format "$%,.2f" (double amount)))

(defn get-related
  [current-product-index related-unshifted]
  (concat (subvec related-unshifted (inc current-product-index))
          (subvec related-unshifted 0 current-product-index)))

(defn product
  [req]
  (let [category-slug (-> req :params :category)
        subcategory-slug (-> req :params :subcategory)
        product-slug (-> req :params :product)
        product (first (pg/execute db/conn
                                   "select * from products where slug like $1"
                                   {:params [product-slug]}))
        related-unshifted (pg/execute db/conn
                                      "select * from products where subcategory_slug like $1 order by name"
                                      {:params [subcategory-slug]})
        related-products (get-related
                           (.indexOf (mapv :slug related-unshifted) (:slug product))
                           related-unshifted)]
    [:div.container.p-4
     [:h1.border-t-2.pt-1.text-xl.font-bold.text-accent1 (:name product)]
     [:div.flex.flex-col.gap-2
      [:div.flex.flex-row.gap-2
       [:img.h-56.w-56.flex-shrink-0.border-2.md:h-64.md:w-64
        {:alt (format "A small picture of %s" (:name product))
         :loading "eager"
         :width "256"
         :height "256"
         :decoding "async"
         :data-nimg "1"
         :style "color: transparent;"
         :src (img/local-image-url (:image_url product) 256)}]
       [:p.flex-grow.text-base (:description product)]]
      [:p.text-xl.font-bold (format-price (:price product))]
      [:form.flex.flex-col.gap-2
       {:action ""}
       [:input {:type "hidden" :value "acrylic-mini-canvas-set-1" :name "productSlug"}]
       [:button.bg-accent1.px-5.py-1.text-sm.font-semibold.text-white
        {:type "submit" :class "max-w-[150px] rounded-[2px]"}
        "Add to cart"]]]
     [:div]
     [:div.pt-8
      [:h2.text-lg.font-bold.text-accent1 "Explore more products"]
      (products/products-list category-slug subcategory-slug related-products)]]))

(defn page
  [req]
  (layout/render-page
    {:content (product req)}))
