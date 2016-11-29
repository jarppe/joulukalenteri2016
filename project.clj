(defproject joulukalenteri "0.0.2016"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 ; Development server:
                 [ring/ring-core "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 ; Frontend deps:
                 [alandipert/storage-atom "2.0.1"]]

  :plugins [[lein-pdo "0.1.1"]
            [lein-cljsbuild "1.1.3"]
            [lein-less "1.7.5"]
            [lein-ring "0.10.0"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources" "target/dev/resources"]
  :clean-targets ^{:replace true :protect false} ["target/dev"]

  :ring {:handler mjk/handler}
  :less {:source-paths ["src/less"]
         :target-path "target/dev/resources"}

  :profiles {:dist {:clean-targets ^{:replace true :protect false} ["target/dist"]
                    :auto-clean false
                    :less {:target-path "target/dist/resources"}
                    :resource-paths ^:replace ["resources" "target/dist/resources"]}}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:main frontend.main
                                   :output-to "target/dev/resources/js/app.js"
                                   :output-dir "target/dev/resources/js/out"
                                   :asset-path "js/out"
                                   :source-map-timestamp true
                                   :closure-defines {goog.DEBUG true}}}
                       {:id "dist"
                        :source-paths ["src/cljs"]
                        :compiler {:main frontend.main
                                   :output-to "target/dist/resources/js/app.js"
                                   :asset-path "js"
                                   :optimizations :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}]}

  :aliases {"dev" [["do"
                    "clean"
                    ["pdo"
                     ["cljsbuild" "auto" "dev"]
                     ["less" "auto"]
                     ["ring" "server-headless"]]]]
            "dist" [["with-profile" "dist"]
                    ["do"
                     "clean"
                     ["less" "once"]
                     ["cljsbuild" "once" "dist"]]]})
