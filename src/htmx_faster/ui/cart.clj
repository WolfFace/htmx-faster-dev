(ns htmx-faster.ui.cart
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]
    [hiccup.util :as h-util]
    [hiccup2.core :as hiccup]
    [htmx-faster.db :as db]
    [pg.core :as pg])
  (:import
    (java.util Base64)))

(defn encode [^String to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn decode [^String to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn set-cart-cookie-header
  [cart]
  (str (format "cart=%s;" (encode (json/write-str cart)))
       "Path=/;"
       "HttpOnly;"
       "Secure;"
       "SameSite=Strict;"
       "Max-Age=86400;"))

(defn decode-cart-cookie
  [req]
  (if-let [cart-base64 (get-in req [:cookies "cart" :value])]
    (json/read-str (decode cart-base64))
    {}))

(defn add-product-to-cart
  [cart product-slug]
  (if (contains? cart product-slug)
    (update cart product-slug inc)
    (assoc cart product-slug 1)))

(defn remove-product-from-cart
  [cart product-slug]
  (dissoc cart product-slug))

(defn get-cart-products
  [cart]
  (if (seq cart)
    (let [??? (str/join ", " (map-indexed (fn [k _v] (str "$" (inc k))) (keys cart)))]
      (pg/execute
        db/conn
        (format "SELECT p.*, s.*, sc.*, c.*
                 FROM products p
                 INNER JOIN subcategories s ON p.subcategory_slug = s.slug
                 INNER JOIN subcollections sc ON s.subcollection_id = sc.id
                 INNER JOIN categories c ON sc.category_slug = c.slug
                 WHERE p.slug in (%s)" ???)
        {:params (keys cart)}))
    []))

;;
;; View/Handler
;;

(defn amount-badge-html
  [cart & [oob?]]
  (let [amount (apply + (vals cart))]
    [:div.absolute.-right-3.-top-1.rounded-full.bg-accent2.px-1.text-xs.text-accent1
     (cond-> {:id "cart-amount"
              :class (if (= 0 amount) "hidden" "")}
             oob? (assoc :hx-swap-oob "true"))
     amount]))

(defn add-to-cart-button
  [product]
  [:form.flex.flex-col.gap-2
   {:hx-post "/cart/add"
    :hx-target "#cart-add-response"
    :hx-swap "outerHTML"
    :hx-sync "closest form:queue all"}
   [:input {:type "hidden" :value (:slug product) :name "product-slug"}]
   [:button.bg-accent1.px-5.py-1.text-sm.font-semibold.text-white
    {:type "submit" :class "max-w-[150px] rounded-[2px]"}
    "Add to cart"]
   [:div.relative
    [:p#cart-add-response (h-util/raw-string "&nbsp;")]
    [:p#cart-add-loading.htmx-indicator.absolute.top-0.bg-white.w-full.opacity-0 "Adding to cart..."]]])

(defn add-to-cart-handler
  [req]
  (let [product-slug (get-in req [:params "product-slug"])
        cart (-> (decode-cart-cookie req)
                 (add-product-to-cart product-slug))]
    {:status 200
     :body (str (hiccup/html [:p#cart-add-response {:hx-swap-oob "true"} "Item added to cart"])
                "\n"
                (hiccup/html (amount-badge-html cart true)))
     :headers {"Set-Cookie" (set-cart-cookie-header cart)
               "Content-Type" "text/html"}}))

(defn amount-badge
  [req]
  (amount-badge-html (decode-cart-cookie req)))

(defn cart-total-price
  [cart products & [oob?]]
  (let [total-price
        (->> products
             (map #(* (get cart (:slug %)) (:price %)))
             (reduce + 0))]
    [:p#cart-total-price.font-semibold
     (if oob? {:hx-swap-oob "true"} {})
     "Merchandise"
     " "
     [:span (format "$%,.2f" (double total-price))]]))
