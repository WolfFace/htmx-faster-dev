(ns htmx-faster.ui.sidebar
  (:require
    [htmx-faster.db :as db]
    [pg.core :as pg]))

(defn sidebar
  []
  [:aside.fixed.left-0.hidden.w-64.min-w-64.max-w-64.overflow-y-auto.border-r.p-4.md:block
   [:h2.border-b.border-accent1.text-sm.font-semibold.text-accent1 "Choose a Category"]
   [:ul.flex.flex-col.items-start.justify-center
    (into [:li.w-full]
          (->> (pg/query db/conn "select * from collections order by name")
               (map (fn [collection]
                      [:li.w-full
                       [:a.block.w-full.py-1.text-xs.text-gray-800.hover:bg-accent2.hover:underline
                        {:href (format "/collection/%s" (:slug collection))
                         :preload "mouseover" :preload-images "true"}
                        (:name collection)]]))))]])
