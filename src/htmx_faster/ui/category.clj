(ns htmx-faster.ui.category
  (:require
    [clojure.string :as str]
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

(defn page
  [req]
  (let [category-slug (-> req :params :category)
        category (first (pg/execute db/conn "select * from categories where slug like $1" {:params [category-slug]}))
        subcategories (pg/execute
                        db/conn
                        "select sc.*
                         from subcategories as sc
                         left join subcollections as scoll on sc.subcollection_id = scoll.id
                         where scoll.category_slug like $1"
                        {:params [category-slug]})
        examples (->> subcategories (take 2) (map :name) (str/join ", "))
        description (format "Choose from our selection of %s, including %s%s and more. In stock and ready to ship."
                            (:name category)
                            examples
                            (if (> (count examples) 1) "," ""))
        subcollections (pg/execute db/conn
                                   "select * from subcollections where category_slug = $1 order by name"
                                   {:params [category-slug]})
        get-subcategories #(pg/execute db/conn
                                       "select * from subcategories where subcollection_id = $1 order by name"
                                       {:params [%]})
        product-count (get-category-product-count category-slug)]
    (layout/render-page
      req
      {:meta (assoc (select-keys category [:name :slug])
               :description description
               :og-image-kind "category")
       :content
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
                                {:class "sm:w-[200px]"
                                 :preload "mouseover" :preload-images "true" :hx-trigger "mousedown"
                                 :href (format "/products/%s/%s" category-slug (:slug subcategory))}
                                [:div.py-2
                                 [:img.h-12.w-12.flex-shrink-0.object-cover
                                  {:alt (format "A small picture of %s" (:name subcategory))
                                   :loading "lazy"
                                   :decoding "async"
                                   :width "48"
                                   :height "48"
                                   :src (img/local-image-url (:image_url subcategory) 81)
                                   :style "color: transparent;"}]]
                                [:div.flex.h-16.flex-grow.flex-col.items-start.py-2
                                 [:div.text-sm.font-medium.text-gray-700.group-hover:underline (:name subcategory)]]])
                             (get-subcategories (:id subcollection))))])
               subcollections))]})))

(defn og-image
  [slug]
  (let [category (first (pg/execute db/conn "select * from categories where slug like $1" {:params [slug]}))
        subcategories (pg/execute
                        db/conn
                        "select sc.*
                         from subcategories as sc
                         left join subcollections as scoll on sc.subcollection_id = scoll.id
                         where scoll.category_slug like $1"
                        {:params [slug]})
        examples (->> subcategories (take 2) (map :name) (str/join ", "))
        description (format "Choose from our selection of %s, including %s%s and more. In stock and ready to ship."
                            (:name category)
                            examples
                            (if (> (count examples) 1) "," ""))
        image-base64 (->> category
                          :image_url
                          img/image-name
                          (img/get-img-base64 256)
                          img/encode-file-to-base64)
        img-src (str "data:image/png;base64," image-base64)]
    [:div
     {:style "display: flex; flex-direction: column; align-items: center; justify-content: center;"}
     [:div
      {:style "display: flex; align-items: center; justify-content: center; margin-bottom: 20px;"}
      [:div
       {:style "width: 200px; height: 200px; display: flex; align-items: center; justify-content: center;"}
       [:img
        {:style "width: 300px; margin-bottom: 30px;"
         :src img-src
         :alt (:name category)}]]]
     [:h1
      {:style "font-size: 64px; font-weight: bold; color: #333; margin-bottom: 20px;"}
      (:name category)]
     [:div
      {:style "text-align: center; font-size: 24px;"}
      description]]))
