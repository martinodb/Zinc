(defproject org.clojars.martinodb/zinc "1.0.0-SNAPSHOT"
  :description "An unofficial fork to turn CSNePS into a library"
  :url "https://github.com/martinodb/Zinc"
  :license {:name "University at Buffalo Public License - v 1.0"
            :url "https://cse.buffalo.edu/sneps/ubpl.pdf"
            :distribution :repo
            :comments "same as CSNePS"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.match "0.2.1"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 
                 
                 
                 
                 [junit/junit "3.8.2"]
                 
                 
                 
                 ;;; this is new
                 [org.clojure/tools.cli "0.4.1"]
                 [reply/reply "0.4.3"]
                 ;;;;;;;
                 
                 
                 ]
  
  :source-paths ["src/clj/"]
  :source-path "src/clj/"
  :java-source-paths ["src/jvm/"] ;leiningen 2 compat.
  :java-source-path "src/jvm/" ;leiningen 1.x compat.
  ;:project-init (require 'clojure.pprint) 
  :repl-options {:init-ns zinc.core.snuser
                  :print clojure.core/println} ; :print clojure.pprint/pprint
                 
  :jvm-opts ["-server"] 
  ;:main zinc.core.snuser
  
  
  
  ;:local-repo  "../../bobtailbot/local-m2"
  
  
  )
