(ns shortify.client 
  (:require [clj-http.client :as http] 
            [clojure.string :as str] 
            [cheshire.core :as json]) 
  (:import (java.net URLEncoder))
  (:gen-class))
                    
(def base-url "http://localhost:4000")

(defn- process-json-response [response success-fn error-fn & {:keys [expected-status] :or {expected-status 200}}] 
  (if (= (:status response) expected-status) 
    (try (let [body-str (:body response)] 
           (if (and body-str (not (str/blank? body-str)))
             (let [parsed-body (json/parse-string body-str true)] 
               (success-fn parsed-body response)) 
             (success-fn nil response)))
         (catch Exception e 
           (println "Ошибка парсинга JSON ответа:" (.getMessage e)) 
           (println "Тело ответа:" (:body response)))) 
    (error-fn response)))

(defn- default-error-handler [response] 
  (try (let [body-str (:body response)] 
         (if (and body-str (not (str/blank? body-str))) 
           (let [parsed-body (json/parse-string body-str true)] 
             (println "Ошибка сервера:" (:status response) "-" (or (:error parsed-body) (:message parsed-body) (pr-str parsed-body)))) 
           (println "Ошибка сервера:" (:status response) "- (пустое тело ошибки)"))) 
       (catch Exception e 
         (println "Ошибка сервера:" (:status response) "- (не удалось распарсить тело ошибки)") (println "Тело ответа:" (:body response)))))
                    
(defn- url-encode [s] (URLEncoder/encode s "UTF-8"))
                    
(defn- create-short-url [] 
  (print "Введите оригинальный URL: ")
  (flush) 
  (let [original-url (read-line)] 
    (if (str/blank? original-url) 
      (println "URL не может быть пустым.") 
      (try 
        (let [encoded-url (url-encode original-url) 
              response (http/post (str base-url "/" encoded-url) 
                                  {:accept :json :throw-exceptions false})] 
          (process-json-response response 
                                 (fn [body _] (println "Создан короткий URL:" (:code body) "для" (:url body))) 
                                 default-error-handler 
                                 :expected-status 201)) 
        (catch Exception e (println "Ошибка при создании URL:" (.getMessage e)))))))
                    
(defn- show-original-url [] 
  (print "Введите короткий URL (код): ")
  (flush) 
  (let [short-code (read-line)] 
    (if (str/blank? short-code) 
      (println "Короткий URL не может быть пустым.") 
      (try 
        (let [encoded-short-code (url-encode short-code) 
              response (http/get (str base-url "/" encoded-short-code) 
                                 {:accept :json :throw-exceptions false})] 
          (process-json-response response 
                                 (fn [body _] (println "Оригинальный URL:" (:url body))) 
                                 (fn [resp] 
                                   (if (= (:status resp) 404) 
                                     (println "Короткий URL не найден.") 
                                     (default-error-handler resp))))) 
        (catch Exception e (println "Ошибка при получении URL:" (.getMessage e)))))))

(defn- update-existing-url [] 
  (print "Введите короткий URL (код) для изменения: ")
  (flush) 
  (let [short-code (read-line)] 
    (if (str/blank? short-code) 
      (println "Короткий URL не может быть пустым.") 
      (do 
        (print "Введите НОВЫЙ оригинальный URL: ")
        (flush) 
        (let [new-original-url (read-line)] 
          (if (str/blank? new-original-url) 
            (println "Новый URL не может быть пустым.") 
            (try 
              (let [encoded-short-code (url-encode short-code) 
                    encoded-new-url (url-encode new-original-url) 
                    response (http/put (str base-url "/" encoded-short-code "/" encoded-new-url)
                                       {:accept :json :throw-exceptions false})] 
                (if (= (:status response) 200) 
                  (println "URL успешно обновлен.") 
                  (default-error-handler response))) 
              (catch Exception e (println "Ошибка при обновлении URL:" (.getMessage e))))))))))

(defn- delete-existing-url [] 
  (print "Введите короткий URL (код) для удаления: ")
  (flush) 
  (let [short-code (read-line)] 
    (if (str/blank? short-code) 
      (println "Короткий URL не может быть пустым.") 
      (try 
        (let [encoded-short-code (url-encode short-code) 
              response (http/delete (str base-url "/" encoded-short-code) 
                                    {:accept :json :throw-exceptions false})] 
          (if (= (:status response) 204) 
            (println "URL успешно удален.") 
            (default-error-handler response))) 
        (catch Exception e (println "Ошибка при удалении URL:" (.getMessage e)))))))

(defn display-menu [] 
  (println "\n------") 
  (println "1. Создать короткий URL") 
  (println "2. Показать оригинальный URL") 
  (println "3. Изменить URL") 
  (println "4. Удалить URL") 
  (println "5. Выйти")
  (print "Выберите действие: ")
  (flush))

(defn -main [& _args] 
  (loop [] 
    (display-menu) 
    (let [choice (read-line)] 
      (case choice 
        "1" (create-short-url) 
        "2" (show-original-url) 
        "3" (update-existing-url) 
        "4" (delete-existing-url) 
        "5" (println "Выход...") 
        (println "Неверный выбор, попробуйте снова.")) 
      (when (not= choice "5") 
        (recur)))))