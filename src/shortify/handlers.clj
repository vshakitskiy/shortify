(ns shortify.handlers
  (:require [compojure.core :refer [GET, POST, PUT, DELETE, defroutes]]
            [compojure.route :refer [not-found]]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]] 
            [cheshire.core :as json]
            [shortify.db :as db]
            [clojure.string :as str]
            ))

(defn json-response [data & {:keys [status] :or {status 200}}]
  (-> (resp/response (json/generate-string data))
      (resp/status status)
      (resp/content-type "application/json; charset=utf-8")))

(defn- create-url-handler [url]
  (let [url (if (str/starts-with? url "/") (subs url 1) url)]
    (if (clojure.string/blank? url)
      (json-response {:error "URL is missing or empty"} :status 400)
      (if-let [short-code (db/create-short-url url)]
        (json-response {:code short-code :url url} :status 201)
        (json-response {:error "failed to create short URL"} :status 500)))))

(defn- get-url-handler [short-url-param]
  (if-let [res (db/get-original-url short-url-param)]
    (json-response {:url (:url res)} :status 200)
    (json-response {:error "short URL not found"} :status 404)))

(defn- update-url-handler [short-url url]
  (if (str/blank? url)
    (json-response {:error "New url cannot be empty"} :status 400)
    (if (db/update-short-url short-url url)
      (-> (resp/response nil) (resp/status 204))
      (json-response {:error "short URL not found or no change made"} :status 404))))

(defn- delete-url-handler [short-url]
  (if (db/delete-short-url short-url)
    (-> (resp/response nil) (resp/status 204))
    (json-response {:error "short URL not found"} :status 404)))

(defn- ping []
  (json-response { :message "pong" }))

(defn- echo [to-echo]
  (json-response { :echo to-echo }))

(defroutes app-routes
  (GET "/ping" _req (ping))
  (GET "/echo/:to-echo" [to-echo] (echo to-echo))
  (POST "/*" [*] (create-url-handler *))
  (GET "/:normal-url" [normal-url] (get-url-handler normal-url))
  (PUT "/:short-url/*" [short-url *] (update-url-handler short-url *))
  (DELETE "/:short-url" [short-url] (delete-url-handler short-url))
  (not-found (json-response {:error "unknown endpoint"} :status 404)))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in api-defaults [:security :anti-forgery] false))))