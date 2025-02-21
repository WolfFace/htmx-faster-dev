(ns htmx-faster.ui.category
  (:require
    [hiccup2.core :as hiccup]
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.layout :as layout]
    [pg.core :as pg]))

(defn get-category-product-count
  [category-slug]
  (:count
    (first
      (pg/execute
        db/conn
        "select count(*) from categories
         left join subcollections on categories.slug = subcollections.category_slug
         left join subcategories on subcollections.id = subcategories.subcollection_id
         left join products on subcategories.slug = products.subcategory_slug
         where categories.slug = $1"
        {:params [category-slug]}))))

(defn category
  [req]
  (let [category-slug (-> req :params :category)
        subcollections (pg/execute db/conn
                                   "select * from subcollections where category_slug = $1 order by name"
                                   {:params [category-slug]})
        get-subcategories #(pg/execute db/conn
                                       "select * from subcategories where subcollection_id = $1 order by name"
                                       {:params [%]})
        product-count (get-category-product-count category-slug)]
    [:div.container.p-4
     [:h1.mb-2.border-b-2.text-sm.font-bold
      (format "%s %s" product-count (if (= product-count 1) "Product" "Products"))]
     (into
       [:div.space-y-4]
       (map (fn [subcollection]
              [:div
               [:h2.mb-2.border-b-2.text-lg.font-semibold (:name subcollection)]
               (into [:div.flex.flex-row.flex-wrap.gap-2]
                     (map (fn [subcategory]
                            [:a.group.flex.h-full.w-full.flex-row.gap-2.border.px-4.py-2.hover:bg-gray-100
                             {:class "sm:w-[200px]" :preload "mouseover" :preload-images "true" :href (format "/products/%s/%s" category-slug (:slug subcategory))}
                             [:div.py-2
                              [:img.h-12.w-12.flex-shrink-0.object-cover
                               {:alt (format "A small picture of %s" (:name subcategory))
                                :loading "eager"
                                :decoding "async"
                                :width "48"
                                :height "48"
                                :src (img/local-image-url (:image_url subcategory) 81)
                                :style "color: transparent;"}]]
                             [:div.flex.h-16.flex-grow.flex-col.items-start.py-2
                              [:div.text-sm.font-medium.text-gray-700.group-hover:underline (:name subcategory)]]])
                          (get-subcategories (:id subcollection))))])
            subcollections))]))

(defn render-page
  [req]
  {:status 200
   :body (str
           "<!DOCTYPE html>"
           \newline
           (hiccup/html (layout/layout (category req))))
   :headers {"Cache-Control" "max-age=10"
             "Content-Type" "text/html;charset=utf-8"}})
