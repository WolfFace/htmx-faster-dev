(ns htmx-faster.ui.products
  (:require
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.layout :as layout]
    [pg.core :as pg]))

(defn products-list
  [category-slug subcategory-slug products]
  (into
    [:div.flex.flex-row.flex-wrap.gap-2]
    (map (fn [product]
           [:a.group.flex.w-full.flex-row.border.px-4.py-2.hover:bg-gray-100
            {:class "h-[130px] sm:w-[250px]"
             :preload "mouseover" :preload-images "true"
             :href (format "/products/%s/%s/%s" category-slug subcategory-slug (:slug product))}
            [:div.py-2
             [:img.h-auto.w-12.flex-shrink-0.object-cover
              {:alt (format "A small picture of %s" (:name product))
               :loading "lazy"
               :width "48"
               :height "48"
               :decoding "async"
               :data-nimg "1"
               :style "color: transparent;"
               :src (img/local-image-url (:image_url product) 81)}]]
            [:div.px-2]
            [:div.h-26.flex.flex-grow.flex-col.items-start.py-2
             [:div.text-sm.font-medium.text-gray-700.group-hover:underline (:name product)]
             [:p.overflow-hidden.text-xs (:description product)]]])
         products)))

(defn products
  [req]
  (let [category-slug (-> req :params :category)
        subcategory-slug (-> req :params :subcategory)
        products (pg/execute db/conn
                             "select * from products where subcategory_slug like $1 order by name"
                             {:params [subcategory-slug]})
        product-count (count products)]
    [:div.container.mx-auto.p-4
     [:h1.mb-2.border-b-2.text-sm.font-bold
      (if (> product-count 0)
        (format "%s %s" product-count (if (= product-count 1) "Product" "Products"))
        "No products for this subcategory")]
     (products-list category-slug subcategory-slug products)]))


(defn cache-products
  [_req]
  (let [products (pg/execute db/conn "select distinct image_url from products")]
    [:div.container.mx-auto.p-4
     [:h1.mb-2.border-b-2.text-sm.font-bold "16 Products"]
     (into
       [:div.flex.flex-row.flex-wrap.gap-2]
       (map (fn [product]
              [:div.py-2
               [:img.h-auto.w-12.flex-shrink-0.object-cover
                {:alt (format "A small picture of %s" (:name product))
                 :loading "lazy"
                 :width "48"
                 :height "48"
                 :decoding "async"
                 :data-nimg "1"
                 :style "color: transparent;"
                 :src (img/local-image-url (:image_url product) 256)}]])
            products))]))

(defn page
  [req]
  (layout/render-page
    req
    {:content (products req)}))

(defn cache-page
  [req]
  (layout/render-page
    req
    {:content (products req)}))
