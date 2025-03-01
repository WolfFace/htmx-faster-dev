(ns htmx-faster.og.core
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell :as sh]
    [hiccup2.core :as hiccup]
    [htmx-faster.ui.category :as category]
    [htmx-faster.ui.product :as product]
    [htmx-faster.ui.products :as products])
  (:import
    (java.nio.file Files)
    (java.util Base64)))

(defn encode [^String to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn render-hiccup
  [hiccup]
  (let [html-base64-str (-> (hiccup/html [:div {:style "padding-top: 100px"} hiccup])
                            str
                            encode
                            (->> (str "data:text/html;base64,")))
        temp-file-name (str (random-uuid) ".png")]
    ;; servo wrapped with good old flock linux tool
    ;; to make this task locked to prevent multiple servo processing
    ;; damn simple but works
    (sh/sh "servo-script.sh"
           "--headless"
           "--window-size" "1200x630"
           "--screen-size" "1200x630"
           html-base64-str
           "-o" temp-file-name)
    (let [file (io/file temp-file-name)
          response-bytes (-> file .toPath Files/readAllBytes)]
      (io/delete-file file)
      response-bytes)))

(defn resolve-og
  [kind slug]
  (case kind
    "product"
    {:hiccup (product/og-image slug)}

    "subcategory"
    {:hiccup (products/og-image slug)}

    "category"
    {:hiccup (category/og-image slug)}

    "home"
    {:image (-> "static/og-homepage.png" io/resource io/input-stream)}))

(defn handler
  [req]
  (let [kind (:kind (:params req))
        slug (:slug (:params req))
        og-params (resolve-og kind slug)]
    {:status 200
     :body (cond
             (:hiccup og-params)
             (render-hiccup (:hiccup og-params))

             (:image og-params)
             (:image og-params))
     :headers {"Content-Type" "image/png"}}))
