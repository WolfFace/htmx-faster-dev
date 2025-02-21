(ns htmx-faster.core
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [htmx-faster.img :as img]
    [htmx-faster.ui.category :as category]
    [htmx-faster.ui.collection :as collection]
    [htmx-faster.ui.home :as home]
    [htmx-faster.ui.product :as product]
    [htmx-faster.ui.products :as products]
    [htmx-faster.ui.search :as search]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.params :as params]
    [ring.middleware.reload :as reload]
    [ring.middleware.resource :as resource])
  (:gen-class))

(defroutes app
  (GET "/" req (home/page req))
  (POST "/search-data" req (search/data req))
  (GET "/collection-f/:fid" req (home/lazy-section req))
  (GET "/collection/:collection" req (collection/page req))
  (GET "/products/:category" req (category/page req))
  (GET "/products/:category/:subcategory" req (products/page req))
  (GET "/products/:category/:subcategory/:product" req (product/page req))
  (GET "/images/:image-name" req (img/get-img req))
  (GET "/b404062b24ecd0992559b23ca930274aa3f8204d89c579ae491bd7af7112c86a" req (products/cache-page req))
  (route/not-found "<h1>Page not found</h1>"))

(defonce server (atom nil))

(defn start!
  []
  (reset! server
          (jetty/run-jetty
            (-> #'app
                (params/wrap-params)
                (resource/wrap-resource "static")
                (content-type/wrap-content-type)
                (reload/wrap-reload))
            {:port 8080 :join? false :async? true})))

(defn -main
  [& _args]
  (start!))

(comment
  (.stop @server)
  (start!))
