(ns htmx-faster.img
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [htmx-faster.db :as db]
    [image-resizer.core :as resize]
    [image-resizer.format :as image-format]
    [pg.core :as pg])
  (:import
    (java.io ByteArrayOutputStream)
    (java.net URL)))

(defn image-name
  [url]
  (format "%s.png" (last (str/split url #"/"))))

;(defn local-image-url
;  [image-url width]
;  (format "/image?q=%s&w=%s" (image-name image-url) width))

(defn local-image-url
  [image-url width]
  (if width
    (format "https://htmx-faster.b-cdn.net/images/%s__%s" width (image-name image-url))
    (format "https://htmx-faster.b-cdn.net/images/%s" (image-name image-url))))

(def mime-type "image/jpeg")

(defn input-stream->byte-array
  [input-stream]
  (let [buffer (byte-array 4096)
        out (ByteArrayOutputStream.)]
    (loop []
      (let [size (.read input-stream buffer)]
        (when (pos? size)
          (.write out buffer 0 size)
          (recur))))
    (.toByteArray out)))

(def resized-image
  (memoize (fn [{:keys [n w]}]
             (->
               (io/file (format ".images/%s" n))
               (resize/resize-to-width w)
               (image-format/as-stream-by-mime-type mime-type)
               (input-stream->byte-array)))))

(defn get-img-new
  [req]
  (let [[img-width img-name] (str/split (-> req :params :image-name) #"__")]
    (if (and img-width img-name)
      {:status 200
       :body (io/file (format ".webp/%s__%s"
                              img-width
                              (-> (str/join "" (drop-last 4 img-name))
                                  (str ".webp"))))
       :headers {"Content-Type" "image/webp"
                 "Cache-Control" "max-age=31536000"}}
      {:status 400
       :body "No image width"
       :headers {"Content-Type" "text/plain"}})))

;;
;; Init image local store
;;

(defn download-file
  [url-str local-path]
  (try
    (with-open [in (io/input-stream (URL. url-str))
                out (io/output-stream local-path)]
      (io/copy in out)
      true)
    (catch Exception e
      (println "Error downloading file:" (.getMessage e))
      false)))

(defn file-name
  [url]
  (last (str/split url #"/")))

(defn image-save
  [url]
  (download-file
    url
    (format ".images/%s.png" (file-name url))))

(defn image-loaded?
  []
  (let [expect-count (count (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
        actual-count (count (filter #(.isFile %) (file-seq (clojure.java.io/file ".images"))))]
    (= expect-count actual-count)))

(defn image-load!
  []
  (->> (map :image_url (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
       (pmap image-save)
       (doall)))

(comment

  (image-loaded?)

  (->> (map :image_url (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
       (pmap image-save)
       (doall))

  (->> (map :image_url (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
       (pmap img/image-name)
       (pmap (fn [n]
               (img/resized-image {:n n :w 256}))))

  (->> (map :image_url (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
       (pmap img/image-name)
       (pmap (fn [n]
               (img/resized-image {:n n :w 48}))))

  (image-save (first (map :image_url (pg/query db/conn "select * from categories order by collection_id"))))

  (download-file
    (first (map :image_url (pg/query db/conn "select * from categories order by collection_id")))
    ".images/save.png")

  (time (do (doall (map slurp (map :image_url (pg/query db/conn "select * from categories order by collection_id")))) nil))
  (time (do (doall (pmap slurp (map :image_url (pg/query db/conn "select * from categories order by collection_id")))) nil))
  (home)

  (count (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))

  (->> (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products")
       (map :image_url)
       (clojure.string/join "\n")
       (spit "urls.txt"))

  (comment))