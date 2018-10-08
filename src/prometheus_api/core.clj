(ns prometheus-api.core
  (:require [clj-http.client :as http]))

;; Error checking?
(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")
        resp (http/get url
                       {:query-params query-params
                        :multi-param-style :array
                        :as :json
                        :debug? false})]
    (:body resp)))

(defn- query [base-url query-params]
  (let [url (str base-url "/api/v1/query")
        resp (http/get url
                       {:query-params query-params
                        :multi-param-style :array
                        :as :json
                        :debug? false})]
    (:body resp)))

(defn -main
  [& args]
  (let [url "http://localhost:9090"]
    [(series url {:match ["up"]})
     (query url {:query "up"})]))
