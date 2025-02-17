(ns htmx-faster.ui.home
  (:require
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [pg.core :as pg]))

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
               [:div.mpsec]
               (mapv
                 (fn [category]
                   [:a.mplink
                    {:href (format "/products/%s" (:slug category)) :preload "mouseover" :preload-images "true"}
                    [:img.mpimg
                     {:alt (format "A small picture of %s" (:name category))
                      :loading "lazy"
                      :width "48"
                      :height "48"
                      :decoding "sync"
                      ;:style "color: transparent;"
                      :src (img/local-image-url (:image_url category) 81)}]
                    [:span.text-xs (:name category)]])
                 categories))]))
        categories-by-collection))))

(defn render-home
  []
  (home))
