(ns prometheus-api.core
  (:require
   [clj-http.client :as http]
   [clojure.pprint :as pp]))

(defn- exec [url query-params]
  (let [opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}
        resp (http/get url opts)]
    ;; Check for :status = "error" or just for :error in the body:
    (-> resp :body :data)))

(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")]
    (exec url query-params)))

(defn- query [base-url query-params]
  (let [url (str base-url "/api/v1/query")]
    (exec url query-params)))

(defn -main [& args]
  (let [url "http://localhost:9090"
        res {:series (series url {:match ["up"]})
             :query (query url {:query "up"})}]
    res
    #_(pp/pprint res)))
