;;;
;;; Prometheus HTTP API [1]. To start a Prometheus server with docker
;;; issue this command [2]:
;;;
;;;     docker run --name prometheus -p 9090:9090 prom/prometheus:v2.9.2
;;;
;;; [1] https://prometheus.io/docs/prometheus/latest/querying/api/
;;; [2] https://prometheus.io/docs/prometheus/latest/installation/
;;;
(ns prometheus-api.core
  (:require
   [clj-http.client :as http]
   [yaml.core :as yaml]
   [clojure.string :as cs]
   [clojure.pprint :as pp])
  (:gen-class))

;; Execute API call:
(defn- exec [url query-params]
  (let [opts {:query-params query-params
              :multi-param-style :array
              :as :json
              :debug? false}
        resp (http/get url opts)]
    ;; Check for :status = "error" or look for :error in the
    ;; body. Data may be still present even if an error occured:
    (-> resp :body :data)))

;; GET /api/v1/series --- get descriptors for the matching time series
;; ...
(defn- series [base-url query-params]
  (let [url (str base-url "/api/v1/series")]
    (exec url query-params)))

(comment
  (series "http://localhost:9090" {:match ["up"]})
  =>
  [{:__name__ "up", :instance "localhost:9090", :job "prometheus"}])

;; GET /api/v1/query
(defn- query [base-url query-params]
  (let [url (str base-url "/api/v1/query")]
    (exec url query-params)))

(comment
  (query "http://localhost:9090" {:query "up"})
  =>
  {:resultType "vector",
   :result [{:metric {:__name__ "up",
                      :instance "localhost:9090",
                      :job "prometheus"},
             :value [1.539026754038E9 "1"]}]})

;; GET /api/v1/label/<label_name>/values
(defn- label-values [base-url label-name]
  (let [url (str base-url "/api/v1/label/" label-name "/values")]
    (exec url {})))

(comment
  (label-values "http://localhost:9090" "job")
  => ["prometheus"])

;; GET /api/v1/status/config
(defn- status-config [base-url]
  (let [url (str base-url "/api/v1/status/config")]
    (let [config (exec url {})]
      ;; Parse yaml, Prometheus returns text:
      (update config :yaml yaml/parse-string))))

(comment
  ;; Ordered map  is displayed as  an ordinary  map in Cider  repl and
  ;; differently for C-x C-e. Hiere the the traditional form:
  (status-config "http://localhost:9090")
  =>
  {:yaml
   {:global {:scrape_interval "15s", .. ..},
    :alerting {.. ..},
    :scrape_configs [...]}})

;; GET /api/v1/query_range
;; GET /api/v1/targets
;; GET /api/v1/rules
;; GET /api/v1/alerts
;; GET /api/v1/targets/metadata
;; GET /api/v1/alertmanagers
;; GET /api/v1/status/flags

;;
;; Make metric  selector in Prometheus  syntax [1]. See  (comment ...)
;; below  for   usage  examples.   Beware  that   empty  stem/name  is
;; allowed. Labels may  be repeated too. Still not  general enough for
;; !=,  =~,  and  !~  operators.  Although  no  range  selectors  with
;; suffixes as "[5m]" possible here.
;;
;; [1] https://prometheus.io/docs/prometheus/latest/querying/basics/
;;
(defn- make-selector [obj]
  ;; {__name_="http_requests_total",...} is a valid metric selector:
  (if (string? obj)
    (make-selector {:__name__ obj})
    (str "{"
         (cs/join ","
                  (for [[k v] obj]
                    (str (name k) "=" (pr-str v))))
         "}")))

;; For your C-x C-e pleasure:
(comment
  ;; See query examples
  ;; https://prometheus.io/docs/prometheus/latest/querying/examples/
  (make-selector "http_requests_total")
  => "{__name__=\"http_requests_total\"}"
  ;; JSON objects as returned by Prometheus API:
  (make-selector {:__name__ "stem",
                  :instance "localhost:9090",
                  :job "job",
                  :quantile "1"})
  => "{__name__=\"stem\",instance=\"localhost:9090\",job=\"job\",quantile=\"1\"}"
  ;; No name and/or repeated labels:
  (make-selector [[:job "a"] [:job "b"]])
  => "{job=\"a\",job=\"b\"}"
)

(defn -main [& args]
  (let [url "http://localhost:9090"]
    (pp/pprint
     ;; An array for "match" query means to match any, not all:
     (let [ms (series url {:match ["go_gc_duration_seconds" "up"]})]
       (for [m ms :let [s (make-selector m)]]
         (query url {:query (str s "[5m]")}))))))
