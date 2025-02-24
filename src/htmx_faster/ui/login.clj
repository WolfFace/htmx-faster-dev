(ns htmx-faster.ui.login
  (:require
    [buddy.sign.jwt :as jwt]
    [hiccup2.core :as hiccup]
    [htmx-faster.db :as db]
    [pg.core :as pg])
  (:import
    (java.security MessageDigest)))

(def password-salt "0328c696f25d03109d4593dee9d56b9ba1ab8050fe50827308d466872ddcf1d5")
(def jwt-private-key "private_key")

(defn render
  [content]
  {:status 200
   :body (str "<!DOCTYPE html>\n" (hiccup/html content))
   :headers {"Cache-Control" "max-age=0"
             "Content-Type"  "text/html;charset=utf-8"}})

(defn sha256 [string]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes string "UTF-8"))]
    (apply str (map (partial format "%02x") digest))))

(defn hash-password
  [password]
  (sha256 (str (sha256 password) password-salt)))

(defn get-user
  [username password]
  (when username password
    (first (pg/execute db/conn
                       "select * from users where username = $1 and password_hash = $2"
                       {:params [username (hash-password password)]}))))

(defn user-exist?
  [username]
  (seq (pg/execute db/conn "select * from users where username = $1" {:params [username]})))

(defn create-user!
  [username password]
  (pg/execute db/conn
              "insert into users (username, password_hash) values ($1, $2)"
              {:params [username (hash-password password)]})
  (get-user username password))

(defn get-user-jwt
  [user]
  (let [current-time (System/currentTimeMillis)
        expiration-time (+ current-time (* 10 86400000))
        claims {:sub (:username user) :exp expiration-time}]
    (jwt/sign claims jwt-private-key {:alg :hs256})))

(defn set-jwt-cookie-header
  [user-jwt]
  (str (format "jwt_token=%s;" user-jwt)
       "Path=/;"
       "HttpOnly;"
       "Secure;"
       "SameSite=Strict;"
       "Max-Age=864000;"))

(defn remove-jwt-cookie-header
  []
  (str (format "jwt_token=;")
       "Path=/;"
       "HttpOnly;"
       "Secure;"
       "SameSite=Strict;"
       "Max-Age=0;"))

(defn get-request-jwt-token-sub
  [req]
  (when-let [jwt-token (get-in req [:cookies "jwt_token" :value])]
    (let [claims (jwt/unsign jwt-token jwt-private-key {:skip-validation true})]
      (:sub claims))))

(defn reload-page
  []
  [:div {:x-init "window.location.reload();"}])

;;
;; Handler
;;

(defn handler
  [req]
  (let [{:keys [username password submit]} (clojure.walk/keywordize-keys (:params req))
        user (get-user username password)]
    (case submit
      "sign-in"
      (if (not user)
        (render [:div.text-sm.text-red-500 "Invalid username or password. Please try again."])
        (let [user-jwt (get-user-jwt user)]
          {:status 200
           :body (str (hiccup/html (reload-page)))
           :headers {"Set-Cookie" (set-jwt-cookie-header user-jwt)}}))

      "sign-up"
      (if (user-exist? username)
        (render [:div.text-sm.text-red-500 "Username already taken. Please try again."])
        (let [new-user (create-user! username password)
              user-jwt (get-user-jwt new-user)]
          {:status 200
           :body (str (hiccup/html (reload-page)))
           :headers {"Set-Cookie" (set-jwt-cookie-header user-jwt)}}))

      "log-out"
      {:status 200
       :body (str (hiccup/html (reload-page)))
       :headers {"Set-Cookie" (remove-jwt-cookie-header)}})))

;;
;; View
;;

(defn popup-sign-in
  []
  [:div#login-popup
   {:x-init "$nextTick(() => htmx.process(document.getElementById('login-popup')))"
    :style "position: fixed; right: 0px; top: 0px; transform: translate(-0px, 30px); min-width: max-content; will-change: transform; z-index: 50;"}
   [:div.z-50.w-72.rounded-md.border.bg-popover.p-4.text-popover-foreground.shadow-md.outline-none.duration-75.px-8.py-4
    {:role "dialog"
     :tabindex "-1"}
    [:span.text-sm.font-semibold.text-accent1 "Log in"]
    [:form.flex.flex-col.space-y-6
     {:hx-post "/login"
      :hx-trigger "submit"
      :hx-swap "innerHTML"
      :hx-target "#login-response"}
     [:div.flex.flex-col.gap-4
      [:div.mt-1
       [:input#username.h-9.border-gray-500.bg-transparent.text-sm.outline-none.relative.block.w-full.appearance-none.border.px-3.py-2.text-gray-900.placeholder-gray-500.focus:z-10.focus:border-orange-500.focus:outline-none.focus:ring-orange-500.sm:text-sm
        {:class "rounded-[1px]"
         :aria-label "Username"
         :autocapitalize "none"
         :autocomplete "username"
         :spellcheck "false"
         :required ""
         :maxlength "50"
         :placeholder "Username"
         :type "text"
         :name "username"}]]
      [:div
       [:div.mt-1
        [:input#password.h-9.border-gray-500.bg-transparent.text-sm.outline-none.relative.block.w-full.appearance-none.border.px-3.py-2.text-gray-900.placeholder-gray-500.focus:z-10.focus:border-orange-500.focus:outline-none.focus:ring-orange-500.sm:text-sm
         {:class "rounded-[1px]"
          :aria-label "Password"
          :required ""
          :maxlength "100"
          :placeholder "Password"
          :type "password"
          :name "password"}]]]
      [:button.inline-flex.items-center.justify-center.gap-2.whitespace-nowrap.ring-offset-background.transition-colors.focus-visible:outline-none.focus-visible:ring-2.focus-visible:ring-ring.focus-visible:ring-offset-2.disabled:pointer-events-none.disabled:opacity-50.h-9.bg-accent1.px-4.py-2.text-xs.font-semibold.text-white.shadow-sm.hover:bg-accent1.focus:outline-none.focus:ring-2.focus:ring-accent1.focus:ring-offset-2
       {:class "rounded-[1px]"
        :type "submit"
        :name "submit"
        :value "sign-in"}
       "Log In"]
      [:button.inline-flex.items-center.justify-center.gap-2.whitespace-nowrap.ring-offset-background.transition-colors.focus-visible:outline-none.focus-visible:ring-2.focus-visible:ring-ring.focus-visible:ring-offset-2.disabled:pointer-events-none.disabled:opacity-50.hover:bg-accent.hover:text-accent-foreground.h-9.border-accent1.bg-white.px-4.py-2.text-xs.font-semibold.text-accent1
       {:class "rounded-[2px] border-[1px]"
        :type "submit"
        :name "submit"
        :value "sign-up"}
       "Create Login"]]
     [:div#login-response]]]])

(defn popup-log-out
  []
  [:div#login-popup
   {:x-init "$nextTick(() => htmx.process(document.getElementById('login-popup')))"
    :style "position: fixed; right: 0px; top: 0px; transform: translate(-0px, 30px); min-width: max-content; will-change: transform; z-index: 50;"}
   [:div.z-50.w-72.rounded-md.border.bg-popover.p-4.text-popover-foreground.shadow-md.outline-none.duration-75.px-8.py-4
    {:role "dialog"
     :tabindex "-1"}
    [:form.flex.flex-col.space-y-6
     {:hx-post "/login"
      :hx-trigger "submit"
      :hx-swap "innerHTML"
      :hx-target "#login-response"}
     [:div.flex.flex-col.gap-4
      [:button.inline-flex.items-center.justify-center.gap-2.whitespace-nowrap.ring-offset-background.transition-colors.focus-visible:outline-none.focus-visible:ring-2.focus-visible:ring-ring.focus-visible:ring-offset-2.disabled:pointer-events-none.disabled:opacity-50.hover:bg-accent.hover:text-accent-foreground.h-9.border-accent1.bg-white.px-4.py-2.text-xs.font-semibold.text-accent1
       {:class "rounded-[2px] border-[1px]"
        :type "submit"
        :name "submit"
        :value "log-out"}
       "Log Out"]]]
    [:div#login-response.h-0]]])

(defn popup
  [req]
  (let [jwt-token (get-in req [:cookies "jwt_token" :value])]
    (if jwt-token
      (popup-log-out)
      (popup-sign-in))))
