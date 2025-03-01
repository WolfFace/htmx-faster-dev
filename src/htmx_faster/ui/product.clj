(ns htmx-faster.ui.product
  (:require
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.cart :as cart]
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

(defn get-product-by-slug
  [slug]
  (first (pg/execute db/conn "select * from products where slug like $1" {:params [slug]})))

(defn page
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
    (layout/render-page
      req
      {:meta (assoc (select-keys product [:name :description :slug])
               :og-image-kind "product")
       :content
       [:div.container.p-4
        [:h1.border-t-2.pt-1.text-xl.font-bold.text-accent1 (:name product)]
        [:div.flex.flex-col.gap-2
         [:div.flex.flex-row.gap-2
          [:img.h-56.w-56.flex-shrink-0.border-2.md:h-64.md:w-64
           {:alt (format "A small picture of %s" (:name product))
            :loading "lazy"
            :width "256"
            :height "256"
            :decoding "async"
            :data-nimg "1"
            :style "color: transparent;"
            :src (img/local-image-url (:image_url product) 256)}]
          [:p.flex-grow.text-base (:description product)]]
         [:p.text-xl.font-bold (format-price (:price product))]
         (cart/add-to-cart-button product)]
        [:div]
        [:div.pt-8
         [:h2.text-lg.font-bold.text-accent1 "Explore more products"]
         (products/products-list category-slug subcategory-slug related-products)]]})))

(defn og-image
  [product-slug]
  (let [product (first (pg/execute db/conn "select * from products where slug like $1" {:params [product-slug]}))
        image-base64 (->> product
                          :image_url
                          img/image-name
                          (img/get-img-base64 256)
                          img/encode-file-to-base64)
        img-src (str "data:image/png;base64," image-base64)]
    [:div
     {:style "width: 100%; height: 100%; display: flex; background-color: #fff; flex-direction: column; align-items: center; justify-content: center;"}
     [:div
      {:style "display: flex; align-items: center; justify-content: center; margin-bottom: 20px;"}
      [:div
       {:style "width: 200px; height: 200px; display: flex; align-items: center; justify-content: center;"}
       [:img
        {:style "width: 300px; margin-bottom: 30px;"
         :src img-src
         :alt (:name product)}]]]
     [:h1
      {:style "text-align: center; font-size: 64px; font-weight: bold; color: #333; margin-bottom: 20px;"}
      (:name product)]
     [:div
      {:style "display: flex; justify-content: space-around; width: 100%;"}
      [:div
       {:style "text-align: center; display: flex; font-size: 24px;"}
       (:description product)]]
     [:div
      {:style "text-align: center; display: flex; font-size: 24px; margin-top: 10px;"}
      (format "$%,.2f" (double (:price product)))]]))

(comment
  (og-picture "basic-composition-in-illustration")

  (->> "basic-composition-in-illustration"
       get-product-by-slug
       :image_url
       img/image-name
       (img/get-img-base64 256)
       img/encode-file-to-base64))
