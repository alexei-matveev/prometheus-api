(defproject f0bec0d/prometheus-api "0.1.0-SNAPSHOT"
  :description "Prometheus HTTP API"
  :url "https://github.com/alexei-matveev/prometheus-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [io.forward/yaml "1.0.9"]]
  :profiles {:uberjar {:aot :all}}
  :main prometheus-api.core)
