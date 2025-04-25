(ns htmx-faster.ui.search
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]
    [htmx-faster.db :as db]
    [htmx-faster.img :as img]
    [pg.core :as pg]))

(defn search-short
  [search-term]
  (let [search-term* (str search-term "%")]
    (pg/execute
      db/conn
      "SELECT p.*, s.*, sc.*, c.*
       FROM products p
       INNER JOIN subcategories s ON p.subcategory_slug = s.slug
       INNER JOIN subcollections sc ON s.subcollection_id = sc.id
       INNER JOIN categories c ON sc.category_slug = c.slug
       WHERE p.name ILIKE $1
       LIMIT 5"
      {:params [search-term*]})))

(defn search-long
  [search-term]
  (let [search-term* (->> (str/split search-term #"\s+")
                          (remove str/blank?)
                          (map #(str % ":*"))
                          (str/join " & "))]
    (pg/execute
      db/conn
      "SELECT p.*, s.*, sc.*, c.*
       FROM products p
       INNER JOIN subcategories s ON p.subcategory_slug = s.slug
       INNER JOIN subcollections sc ON s.subcollection_id = sc.id
       INNER JOIN categories c ON sc.category_slug = c.slug
       WHERE to_tsvector('english', p.name) @@ to_tsquery('english', $1)
       LIMIT 5"
      {:params [search-term*]})))

(defn query
  [search-term]
  (if (> (count search-term) 2)
    (search-long search-term)
    (search-short search-term)))

(def cross-icon
  [:svg.lucide.lucide-x.absolute.right-7.top-2.h-5.w-5.text-muted-foreground
   {:xmlns "http://www.w3.org/2000/svg"
    :width "24"
    :height "24"
    :viewBox "0 0 24 24"
    :fill "none"
    :stroke "currentColor"
    :stroke-width "2"
    :stroke-linecap "round"
    :stroke-linejoin "round"}
   [:path {:d "M18 6 6 18"}] [:path {:d "m6 6 12 12"}]])

(defn data
  [req]
  (let [search-term (get (:form-params req) "term")
        products (mapv
                   (fn [{:keys [name image_url category_slug subcategory_slug slug]}]
                     {:name name
                      :href (format "/products/%s/%s/%s" category_slug subcategory_slug slug)
                      :image (img/local-image-url image_url 81)})
                   (query search-term))]
    {:status 200
     :body (json/write-str {:term search-term :products products})
     :headers {"Content-Type" "application/json"}}))

(defn alpine-input
  []
  [:div.mx-0.flex-grow.sm:mx-auto.sm:flex-grow-0
   [:div.font-sans
    [:div#search.relative.flex-grow
     {:x-data "{
       loading: false,
       term: '',
       products: [],
       open: false,
       selected: -1,

       htmxFasterSearch(value) {
         const method = `POST`;
         const body = new URLSearchParams({'term': value});
         const headers = {};
         headers['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';

         this.open = true;
         this.term = value;
         this.selected = -1;
         this.loading = true;
         fetch(`/search-data`, {method, body, headers}).then(res => res.json()).then(res => {
           if (this.term === res.term) {
             this.products = res.products;
             this.loading = false;
           }
           $nextTick(() => htmx.process(document.getElementById('search')))
         })
       },

       htmxFasterSearchClear() {
         this.open = false;
         this.term = '';
       }
      }"}
     [:div.relative
      [:input#search-input
       {:class "sm:w-[300px] md:w-[375px]
                flex h-9 w-full border border-gray-500 bg-transparent
                px-3 py-1 text-sm outline-none pr-12 font-sans font-medium"
        :type "text"
        :name "search-term"
        :autocapitalize "none"
        :autocorrect "off"
        :placeholder "Search..."
        :x-on:click "open = true"
        :x-on:click.outside "open = false"
        :x-on:keydown.down.prevent "selected = Math.min(selected + 1, products.length - 1)"
        :x-on:keydown.up.prevent "selected = Math.max(selected - 1, 0)"
        :x-on:keydown.enter "document.getElementById('search').querySelectorAll('a')[selected].click()"
        :x-on:input "htmxFasterSearch($event.target.value)"
        :x-bind:value "term"}]
      [:template
       {:x-if "open && term.length > 0"}
       [:div.clear-input.cursor-pointer
        {:x-on:click "htmxFasterSearchClear()"}
        cross-icon]]]

     [:template
      {:x-if "open && term.length > 0"}
      [:div.absolute.z-10.w-full.border.border-gray-200.bg-white.shadow-lg
       [:div.relative.overflow-hidden
        {:dir "ltr" :class "h-[300px]" :style "position: relative;"}

        [:template
         {:x-if "loading && products.length === 0"}
         [:div#suggest.h-full.w-full
          {:class "rounded-[inherit]"
           :style "overflow: hidden scroll;"}
          [:div.flex.h-full.items-center.justify-center
           [:p.text-sm.text-gray-500
            "Loading..."]]]]
        [:template
         {:x-if "!loading && products.length === 0"}
         [:div#suggest.h-full.w-full
          {:class "rounded-[inherit]"
           :style "overflow: hidden scroll;"}
          [:div.flex.h-full.items-center.justify-center
           [:p.text-sm.text-gray-500
            "No results found"]]]]
        [:template
         {:x-if "products.length > 0"}
         [:div#suggest.h-full.w-full
          [:template
           {:x-for "(product, idx) in products"}
           [:a
            {:x-bind:href "product.href"
             :preload "mouseover" :preload-images "true" :hx-trigger "mousedown"}
            [:div.flex.cursor-pointer.items-center.p-2
             {:x-bind:class "idx === selected && 'bg-gray-100'"
              :x-on:mouseover "selected = idx"}
             [:img.h-10.w-10.pr-2
              {:alt ""
               :loading "lazy"
               :width "40"
               :height "40"
               :decoding "async"
               :style "color: transparent;"
               :x-bind:src "product.image"}]
             [:span.text-sm
              {:x-text "product.name"}]]]]]]]]]]]])
