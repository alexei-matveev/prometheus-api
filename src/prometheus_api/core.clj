(ns prometheus-api.core
  (:require [clj-http.client :as http]))

(defn- exec [url opts]
  (let [resp (http/get url opts)]
    ;; Check for :status = "error" or just for :error in the body:
    (-> resp :body :data)))

(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")
        opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}]
    (exec url opts)))

(defn- query [base-url query-params]
  (let [url (str base-url "/api/v1/query")
        opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}]
    (exec url opts)))

(defn -main
  [& args]
  (let [url "http://localhost:9090"]
    [(series url {:match ["up"]})
     (query url {:query "up"})]))
