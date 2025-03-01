(ns htmx-faster.img
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str])
  (:import
    (java.io ByteArrayOutputStream)
    (org.apache.commons.codec.binary Base64)
    (org.apache.commons.io FileUtils)))

(defn image-name
  [url]
  (format "%s.png" (last (str/split url #"/"))))

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

(defn get-img
  [req]
  (let [[img-width img-name] (str/split (-> req :params :image-name) #"__")]
    (if (and img-width img-name)
      {:status 200
       :body (io/file (format "/.webp/%s__%s"
                              img-width
                              (-> (str/join "" (drop-last 4 img-name))
                                  (str ".webp"))))
       :headers {"Content-Type" "image/webp"
                 "Cache-Control" "max-age=31536000"}}
      {:status 400
       :body "No image width"
       :headers {"Content-Type" "text/plain"}})))

(defn get-img-base64
  [width img-name]
  (io/file (format "/.webp/%s__%s"
                   width
                   (-> (str/join "" (drop-last 4 img-name))
                       (str ".webp")))))

(defn encode-file-to-base64
  [file]
  (try
    (let [encoded (Base64/encodeBase64 (FileUtils/readFileToByteArray file))]
      (String. encoded "US-ASCII"))
    (catch Exception _e "")))
