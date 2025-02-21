(ns htmx-faster.ui.home
  (:require
    [hiccup2.core :as hiccup]
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [htmx-faster.ui.layout :as layout]
    [pg.core :as pg]))

(defn collection-fragment
  [collection-id]
  (let [id->collection (->> (pg/query db/conn "select * from collections")
                            (reduce #(assoc %1 (:id %2) %2) {}))
        collection (id->collection collection-id)
        categories (pg/execute db/conn "select * from categories where collection_id = $1"
                              {:params [collection-id]})]
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
              :decoding "async"
              :style "color: transparent;"
              :src (img/local-image-url (:image_url category) 81)}]
            [:span.text-xs (:name category)]])
         categories))]))

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
      (doall
        (map-indexed
          (fn [idx categories]
            (let [collection-id (-> categories first :collection_id)]
              (if (>= idx 3)
                [:div {:hx-target "this"
                       :hx-swap "outerHTML"
                       :hx-trigger "load"
                       :hx-get (format "/collection-f/%s" collection-id)}]
                (collection-fragment collection-id))))
          categories-by-collection)))))

(defn lazy-section
  [req]
  (let [fid (-> req :params :fid)]
    (str
      "<!DOCTYPE html>"
      \newline
      (hiccup/html (collection-fragment (Integer/parseInt fid))))))

(defn page
  [_req]
  (layout/render-page
    {:content (home)}))
