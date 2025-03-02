(ns htmx-faster.core
  (:require
    [aleph.http :as http]
    [org.httpkit.server :as hk-server]
    [compojure.core :refer :all :as compojure]
    [compojure.route :as route]
    [htmx-faster.img :as img]
    [htmx-faster.og.core :as og]
    [htmx-faster.ui.cart :as cart]
    [htmx-faster.ui.category :as category]
    [htmx-faster.ui.collection :as collection]
    [htmx-faster.ui.home :as home]
    [htmx-faster.ui.login :as login]
    [htmx-faster.ui.order :as order]
    [htmx-faster.ui.order-history :as order-history]
    [htmx-faster.ui.product :as product]
    [htmx-faster.ui.products :as products]
    [htmx-faster.ui.search :as search]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.params :as params]
    [ring.middleware.reload :as reload]
    [ring.middleware.resource :as resource])
  (:gen-class))

(def app
  (compojure/routes
    (GET "/" req (home/page req))
    (POST "/search-data" req (search/data req))
    (POST "/login" req (login/handler req))
    (POST "/cart/add" req (cart/add-to-cart-handler req))
    (POST "/cart/amount" req (cart/amount-badge req))
    (GET "/collection-f/:fid" req (home/lazy-section req))
    (GET "/collection/:collection" req (collection/page req))
    (GET "/products/:category" req (category/page req))
    (GET "/products/:category/:subcategory" req (products/page req))
    (GET "/products/:category/:subcategory/:product" req (product/page req))
    (GET "/images/:image-name" req (img/get-img req))
    (GET "/b404062b24ecd0992559b23ca930274aa3f8204d89c579ae491bd7af7112c86a" req (products/cache-page req))
    (GET "/order-history" req (order-history/page req))
    (GET "/order" req (order/page req))
    (POST "/order/remove" req (order/remove-item-handler req))
    (GET "/og/image/:kind/:slug" req (og/handler req))
    (route/not-found "<h1>Page not found</h1>")))

(defonce server (atom nil))

;(defn start!
;  []
;  (reset! server
;          (jetty/run-jetty
;            (-> #'app
;                (cookies/wrap-cookies)
;                (params/wrap-params)
;                (resource/wrap-resource "static")
;                (content-type/wrap-content-type))
;            {:port 8080 :join? false :async? false})))

(def handler
  (-> app
      (cookies/wrap-cookies)
      (params/wrap-params)
      (resource/wrap-resource "static")
      (content-type/wrap-content-type)))

(defn start-aleph!
  []
  (reset! server
          (http/start-server
            handler
            {:port 8080})))
             ;:use-h2c? true
             ;:http-versions [:http2 :http1]})))

(defn stop-aleph!
  []
  (.close @server))


(defn start-hk!
  []
  (reset! server
          (hk-server/run-server handler {:port 8080})))


(defn stop-hk!
  []
  (@server :timeout 100))

(defn -main
  [& _args]
  (start-hk!))

(comment
  (.stop @server)
  (start!)

  (start-aleph!)
  (stop-aleph!)

  (start-hk!)
  (stop-hk!)


  (do))

