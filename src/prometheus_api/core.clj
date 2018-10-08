(ns prometheus-api.core
  (:require [clj-http.client :as http]))

(defn -main
  [& args]
  (let [resp (http/get "http://localhost:9090/api/v1/series?match[]=up")]
    (println (:body resp))))
