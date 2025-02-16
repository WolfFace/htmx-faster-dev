(ns htmx-faster.ui.home
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [pg.core :as pg])
  (:import (java.net URL)))

(defn home
  []
  (let [id->collection (->> (pg/query db/conn "select * from collections")
                            (reduce #(assoc %1 (:id %2) %2) {}))
        categories-by-collection (->> (pg/query db/conn "select * from categories order by collection_id")
                                      (into [] (partition-by :collection_id))
                                      (sort-by #(-> % first :collection_id id->collection :name)))]
    (into
      [:div.w-full.p-4
       [:div.mb-2.w-full.flex-grow.border-accent1.text-sm.font-semibold.text-black
        {:class "border-b-[1px]"} "Explore 1,008,768 products"]]
      (mapv
        (fn [categories]
          (let [collection (-> categories first :collection_id id->collection)]
            [:div
             [:h2.text-xl.font-semibold (:name collection)]
             (into
               [:div.flex.flex-row.flex-wrap.justify-center.gap-2.border-b-2.py-4.sm:justify-start]
               (mapv
                 (fn [category]
                   [:a.flex.flex-col.items-center.text-center
                    {:class "w-[125px]" :href (format "/products/%s" (:slug category)) :preload "preload:init"}
                    [:img.mb-2.h-14.w-14.border.hover:bg-accent2
                     {:alt (format "A small picture of %s" (:name category))
                      :loading "lazy"
                      :width "48"
                      :height "48"
                      :decoding "sync"
                      :data-nimg "1"
                      :style "color: transparent;"
                      :srcset ""
                      :src (img/local-image-url (:image_url category) 48)}]
                    [:span.text-xs (:name category)]])
                 categories))]))
        categories-by-collection))))

(defn render-home
  []
  (home))
