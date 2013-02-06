(defproject hp-crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.taoensso/timbre "1.3.0"]
                 [slingshot "0.10.3"]
                 [clj-time "0.4.4"]
                 [org.clojure/tools.cli "0.2.2"]
                 [enlive "1.0.0"]
                 [clj-tagsoup "0.3.0"]
                 [net.sourceforge.htmlunit/htmlunit "2.11"]
                 [clj-http "0.5.6"]
                 [com.brweber2/clj-dns "0.0.2"]]
  :dev-dependencies [[lein-marginalia "0.7.1"]]
  :resource-paths ["resources"]
  :main hp-crawler.core
  )
