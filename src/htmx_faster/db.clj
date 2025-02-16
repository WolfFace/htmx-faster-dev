(ns htmx-faster.db
  (:require [pg.core :as pg]))

(def config
  {:host "db"
   :port 5432
   :user "default"
   :password "postgres"
   :database "mydb"})

;(def config
;  {:host "localhost"
;   :port 5432
;   :user "postgres"
;   :password "123123"
;   :database "htmxfaster"})

(def conn
  (try (pg/connect config)
       (catch Exception _ nil)))

(comment

  (pg/query conn "select * from collections"))
