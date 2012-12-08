(defproject to-be-continued "0.1.0"
  :description "Asynchronous programming, simplified."
  :url "https://github.com/gregspurrier/to-be-continued"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "0.2.9"]]
  :hooks [leiningen.cljsbuild]
  :dependencies []
  :source-paths ["src/clj" "src/crossover"]
  :cljsbuild {:crossovers [to-be-continued.fns
                           to-be-continued.macros]
              :crossover-jar true
              :builds [{:source-path "src/cljs"
                        :jar true}]}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.4.0"]
                                  [org.clojure/tools.nrepl "0.2.0-RC1"]
                                  [clojure-complete "0.2.2"]
                                  [midje "1.4.0"]]}})
