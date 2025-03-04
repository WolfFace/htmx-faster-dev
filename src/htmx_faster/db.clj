(ns htmx-faster.db
  (:require
    [pg.core :as pg]))

(def config
  {:host "db"
   :port 5432
   :user "default"
   :password "postgres"
   :database "mydb"})

(def conn
  (try (pg/connect config)
       (catch Exception _ nil)))
