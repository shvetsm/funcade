(defproject funcade "0.1.0-SNAPSHOT"
  :description "Gives you OAuth 2.0 tokens so you can play"
  :url "https://github.com/shvetsm/funcade"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [tolitius//envoy "0.1.8"]
                 [http-kit "2.3.0"]
                 [funcool/cuerdas "2.0.5"]
                 [metosin/jsonista "0.1.1"]
                 [camel-snake-kebab "0.4.0"]])
