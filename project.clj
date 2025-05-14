(defproject shortify "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.14.1"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-defaults "0.3.4"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.0"]
                 [korma "0.4.3"]
                 [org.postgresql/postgresql "42.5.4"]
                 [clj-http "3.12.3"]
                 [cheshire "5.11.0"]]
  :aot [shortify.core shortify.client]
  :profiles {
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :server  {:main shortify.core
                       :uberjar-name "shortify-server.jar"}
             :client  {:main shortify.client
                       :uberjar-name "shortify-client.jar"}}
  :aliases {
          "run-server-dev" ["run" "-m" "shortify.core/-main"]
          "run-client-dev" ["run" "-m" "shortify.client/-main"]

          "package-server" ["with-profile" "+server" "uberjar"]
          "package-client" ["with-profile" "+client" "uberjar"]})