(ns prometheus-api.core
  (:require [clj-http.client :as http]))

;; Error checking?
(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")
        resp (http/get url
                       {:query-params query-params
                        :multi-param-style :array
                        :debug? false})]
    (:body resp)))

(defn -main
  [& args]
  (let [url "http://localhost:9090"
        body (series url {:match ["up"]})]
    body))
