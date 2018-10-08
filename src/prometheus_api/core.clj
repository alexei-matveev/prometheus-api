;;;
;;; Prometheus HTTP API [1]
;;;
;;; [1] https://prometheus.io/docs/prometheus/latest/querying/api/
;;;
(ns prometheus-api.core
  (:require
   [clj-http.client :as http]
   [clojure.string :as cs]
   [clojure.pprint :as pp])
  (:gen-class))

(defn- exec [url query-params]
  (let [opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}
        resp (http/get url opts)]
    ;; Check for :status = "error" or look for :error in the
    ;; body. Data may be still present even if an error occured:
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

;;
;; Make matric  selector in Prometheus  syntax [1]. See  (comment ...)
;; below  for   usage  examples.   Beware  that   empty  stem/name  is
;; allowed. Labels may  be repeated too. Still not  general enough for
;; !=, =~, and !~ operators ...
;;
;; [1] https://prometheus.io/docs/prometheus/latest/querying/basics/
;;
(defn- make-selector
  ([obj]
   (let [stem (:__name__ obj)
         labels (dissoc obj :__name__)]
     (make-selector stem labels)))
  ([stem labels]
   (str stem
        "{"
        (cs/join ","
                 (for [[k v] labels]
                   (str (name k) "=" (pr-str v))))
        "}")))

;; For your C-x C-e pleasure:
(comment
  ;; Basic usage:
  (make-selector "stem" {:label "some value"})
  => "stem{label=\"some value\"}"
  ;; Empty stem and/or repeated labels:
  (make-selector "" [[:job "a"] [:job "b"]])
  => "{job=\"a\",job=\"b\"}"
  ;; JSON objects as returned by Prometheus API:
  (make-selector {:__name__ "stem",
                  :instance "localhost:9090",
                  :job "job",
                  :quantile "1"})
  => "stem{instance=\"localhost:9090\",job=\"job\",quantile=\"1\"}")

(defn -main [& args]
  (let [url "http://localhost:9090"]
    (pp/pprint
     ;; An array for "match" query means to match any, not all:
     (let [ms (series url {:match ["go_gc_duration_seconds" "up"]})]
       (for [m ms :let [s (make-selector m)]]
         (query url {:query s}))))))
