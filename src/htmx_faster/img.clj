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

(defn local-image-url
  [image-url width]
  (format "/image?q=%s&w=%s" (image-name image-url) width))

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

(defn get-img
  [req]
  (let [img-name (-> req :query-params (get "q"))
        img-width (-> req :query-params (get "w"))]
    (cond
      img-width
      {:status 200
       :body (resized-image {:n img-name :w (Integer/parseInt img-width)})
       :headers {"Content-Type" mime-type}}

      :else
      (io/file (format ".images/%s" img-name)))))

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
  [])

(defn image-load!
  []
  (->> (map :image_url (pg/query db/conn "select image_url from categories union select image_url from subcategories union select image_url from products"))
       (pmap image-save)
       (doall)))

(comment

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