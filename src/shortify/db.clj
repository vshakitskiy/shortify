(ns shortify.db
  (:require [korma.db :as kdb]
            [korma.core :as k]))

(def connect-db
  (kdb/postgres {:db (or (System/getenv "DB_NAME") "db")
                 :host (or (System/getenv "DB_HOST") "localhost")
                 :port (Integer/parseInt (or (System/getenv "DB_PORT") "5555"))
                 :user (or (System/getenv "DB_USER") "admeanie")
                 :password (or (System/getenv "DB_PASSWORD") "shabi")}))

(kdb/defdb db connect-db)

(defn urls-table []
  (try
    (k/exec-raw db ["CREATE TABLE IF NOT EXISTS urls (
                     id SERIAL PRIMARY KEY,
                     code TEXT UNIQUE NOT NULL,
                     url TEXT NOT NULL
                     );"])
    (println "urls table sql executed")
    true
    (catch Exception e
      (println (str "Error executing urls table sql: " (.getMessage e)))
      false)))

(k/defentity urls
  (k/pk :id)
  (k/table :urls)
  (k/entity-fields :code :url)
  (k/prepare #(update % :url (fn [s] (if s (clojure.string/trim s) s))))
  (k/transform #(update % :url (fn [s] (if s (clojure.string/trim s) s)))))


(defn generate-short-code []
  (let [chars (map char (concat (range 48 58)
                                (range 65 91)
                                (range 97 123)))
        code-length 6]
    (loop [attempts 0]
      (if (> attempts 10)
        (throw (Exception. "Failed to generate a unique short code after multiple attempts."))
        (let [code (apply str (repeatedly code-length #(rand-nth chars)))]
          (if (empty? (k/select urls
                                (k/fields :id)
                                (k/where {:code code})
                                (k/limit 1)))
            code
            (recur (inc attempts))))))))

(defn create-short-url [original-url]
  (if (or (nil? original-url) (clojure.string/blank? original-url))
    (do
      (println "Original URL cannot be blank.")
      nil)
    (try
      (let [short-code (generate-short-code)] 
        (let [result (k/insert urls
                               (k/values {:code short-code
                                          :url original-url}))]
          (if result short-code nil)))
      (catch Exception e
        (println (str "Error creating short URL: " (.getMessage e)))
        nil))))

(defn get-original-url [short-code]
  (if (clojure.string/blank? short-code)
    nil
    (first (k/select urls
                     (k/fields :url)
                     (k/where {:code short-code})
                     (k/limit 1)))))

(defn update-short-url [short-code new-original-url]
  (if (or (clojure.string/blank? short-code)
          (clojure.string/blank? new-original-url))
    false
    (let [updated-count (k/update urls
                                  (k/set-fields {:url new-original-url})
                                  (k/where {:code short-code}))]
      (> updated-count 0))))

(defn delete-short-url [short-code]
  (if (clojure.string/blank? short-code)
    false
    (let [deleted-count (k/delete urls
                                  (k/where {:code short-code}))]
      (> deleted-count 0))))