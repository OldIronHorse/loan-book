(defproject loan-book "0.1.0-SNAPSHOT"
  :description "Library to implement a loan book to match borrow and lend requests."
  :url "https://github.com/OldIronHorse/loan-book"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [compojure "1.5.0"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler loan-book.handler/app
         :nrepl {:start? true
                 :port 9998}}
  :profiles 
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
