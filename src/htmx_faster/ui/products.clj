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

(defn page
  [req]
  (let [category-slug (-> req :params :category)
        subcategory-slug (-> req :params :subcategory)
        products (pg/execute db/conn
                             "select * from products where subcategory_slug like $1 order by name"
                             {:params [subcategory-slug]})
        subcategory (first (pg/execute
                             db/conn
                              "select * from subcategories where slug like $1"
                              {:params [subcategory-slug]}))
        product-count (count products)]
    (layout/render-page
      req
      {:meta (assoc (select-keys subcategory [:name :slug])
               :description (format "Choose from over %s products in %s. In stock and ready to ship."
                                    (count products)
                                    (:name subcategory))
               :og-image-kind "subcategory")
       :content
       [:div.container.mx-auto.p-4
        [:h1.mb-2.border-b-2.text-sm.font-bold
         (if (> product-count 0)
           (format "%s %s" product-count (if (= product-count 1) "Product" "Products"))
           "No products for this subcategory")]
        (products-list category-slug subcategory-slug products)]})))

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

(defn cache-page
  [req]
  (layout/render-page
    req
    {:content (cache-products req)}))

(defn og-image
  [slug]
  (let [subcategory (first (pg/execute db/conn "select * from subcategories where slug like $1" {:params [slug]}))
        image-base64 (->> subcategory
                          :image_url
                          img/image-name
                          (img/get-img-base64 256)
                          img/encode-file-to-base64)
        img-src (str "data:image/png;base64," image-base64)
        description (format "Choose from our selection of %s. In stock and ready to ship." (:name subcategory))]
    [:div
     {:style "width: 100%; height: 100%; display: flex; background-color: #fff; flex-direction: column; align-items: center; justify-content: center;"}
     [:div
      {:style "display: flex; align-items: center; justify-content: center; margin-bottom: 20px;"}
      [:div
       {:style "width: 200px; height: 200px; display: flex; align-items: center; justify-content: center;"}
       [:img
        {:style "width: 300px; margin-bottom: 30px;"
         :src img-src
         :alt (:name subcategory)}]]]
     [:h1
      {:style "text-align: center; font-size: 64px; font-weight: bold; color: #333; margin-bottom: 20px;"}
      (:name subcategory)]
     [:div
      {:style "display: flex; justify-content: space-around; width: 100%;"}
      [:div
       {:style "text-align: center; display: flex; font-size: 24px;"}
       description]]]))
