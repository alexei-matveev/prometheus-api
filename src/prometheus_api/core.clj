;;;
;;; Prometheus HTTP API [1]
;;;
;;; [1] https://prometheus.io/docs/prometheus/latest/querying/api/
;;;
(ns prometheus-api.core
  (:require
   [clj-http.client :as http]
   [clojure.string :as cs]
   [clojure.pprint :as pp]))

(defn- exec [url query-params]
  (let [opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}
        resp (http/get url opts)]
    ;; Check for :status = "error" look for :error in the body. Data
    ;; may be still present even if the error occured:
    (-> resp :body :data)))

;; GET /api/v1/series
(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")]
    (exec url query-params)))

;; GET /api/v1/query
(defn- query [base-url query-params]
  (let [url (str base-url "/api/v1/query")]
    (exec url query-params)))

;; GET /api/v1/query_range
;; GET /api/v1/label/<label_name>/values
;; GET /api/v1/targets
;; GET /api/v1/rules
;; GET /api/v1/alerts
;; GET /api/v1/targets/metadata
;; GET /api/v1/alertmanagers
;; GET /api/v1/status/config
;; GET /api/v1/status/flags

(defn- make-selector [obj]
  (let [stem (:__name__ obj)
        labels (dissoc obj :__name__)]
    (str stem
         "{"
         (apply str
                (cs/join ","
                         (for [[k v] labels]
                           (str (name k) "=" (pr-str v)))))
         "}")))

(defn -main [& args]
  (let [url "http://localhost:9090"
        res {:series (series url {:match ["up"]})
             :query (query url {:query "up"})}]
    (pp/pprint res)
    (pp/pprint
     (let [ms (series url {:match ["go_gc_duration_seconds"]})]
       (for [m ms]
         (make-selector m))))))
