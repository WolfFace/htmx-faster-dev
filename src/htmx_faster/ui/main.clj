(ns htmx-faster.ui.main
  (:require
    [hiccup2.core :as hiccup]
    [htmx-faster.ui.layout :as layout]
    [htmx-faster.ui.home :as home]))

(defn render-page
  []
  (str
    "<!DOCTYPE html>"
    \newline
    (hiccup/html (layout/layout (home/render-home)))))
