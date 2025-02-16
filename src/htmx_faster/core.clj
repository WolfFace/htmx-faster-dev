(ns htmx-faster.core
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [htmx-faster.img :as img]
    [htmx-faster.ui.category :as category]
    [htmx-faster.ui.collection :as collection]
    [htmx-faster.ui.main :as main]
    [htmx-faster.ui.product :as product]
    [htmx-faster.ui.products :as products]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.params :as params]
    [ring.middleware.reload :as reload]
    [ring.middleware.resource :as resource])
  (:gen-class))

(defroutes app
  (GET "/" [] (main/render-page))
  (GET "/collection/:collection" req (collection/render-page req))
  (GET "/products/:category" req (category/render-page req))
  (GET "/products/:category/:subcategory" req (products/render-page req))
  (GET "/products/:category/:subcategory/:product" req (product/render-page req))
  (GET "/images/:image-name" req (img/get-img-new req))
  (GET "/image" req (img/get-img req))
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
  (when (not (img/image-loaded?))
    (println "Downloading files...")
    (img/image-load!))
  (start!))

(comment
  (.stop @server)
  (start!))
