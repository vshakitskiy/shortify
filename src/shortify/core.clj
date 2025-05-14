(ns shortify.core
  (:require [ring.adapter.jetty :as jetty]
            [shortify.handlers :refer [app]]
            [shortify.db :as db])
  (:gen-class))

(defn -main 
  [& _args]
  (let [db-success (db/urls-table)]
    (if db-success
      (do
        (println "Database initialized successfully")
        (println "Starting server on port 4000")
        (jetty/run-jetty app {:port 4000 :join? true}))
      (do
        (println "Failed to initialize database")
        (System/exit 1)))))