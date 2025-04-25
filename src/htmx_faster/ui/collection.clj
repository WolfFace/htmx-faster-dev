(ns htmx-faster.ui.collection
  (:require
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.layout :as layout]
    [pg.core :as pg]))

(defn collection
  [req]
  (let [collection-slug (-> req :params :collection)
        collection (first (pg/execute db/conn
                                      "select * from collections where slug like $1"
                                      {:params [collection-slug]}))
        categories (pg/execute db/conn
                               "select * from categories where collection_id = $1 order by name"
                               {:params [(:id collection)]})]
    [:div.w-full.p-4
     [:h2.text-xl.font-semibold (:name collection)]
     (into
       [:div.flex.flex-row.flex-wrap.justify-center.gap-2.border-b-2.py-4.sm:justify-start]
       (mapv
         (fn [category]
           [:a.flex.flex-col.items-center.text-center
            {:class "w-[125px]"
             :href (format "/products/%s" (:slug category))
             :preload "mouseover" :preload-images "true" :hx-trigger "mousedown"}
            [:img.mb-2.h-14.w-14.border.hover:bg-accent2
             {:alt (format "A small picture of %s" (:name category))
              :loading "lazy"
              :width "48"
              :height "48"
              :decoding "async"
              :style "color: transparent;"
              :src (img/local-image-url (:image_url category) 81)}]
            [:span.text-xs (:name category)]])
         categories))]))

(defn page
  [req]
  (layout/render-page
    req
    {:content (collection req)}))
