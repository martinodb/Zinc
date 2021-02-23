(defproject org.clojars.martinodb/zinc "1.0.0-SNAPSHOT"
  :description "An unofficial fork to turn CSNePS into a library"
  :url "https://github.com/martinodb/Zinc"
  :license {:name "University at Buffalo Public License - v 1.0"
            :url "https://cse.buffalo.edu/sneps/ubpl.pdf"
            :distribution :repo
            :comments "same as CSNePS"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.match "0.2.2"]
                 [org.clojure/core.memoize "0.7.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/tools.trace "0.7.10"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 
                 
                 
                 [junit/junit "4.12"]
                 
                 ;;; added by martinodb
                 [com.taoensso/timbre "4.10.0"] ; logging
                 [org.clojure/tools.logging "0.4.1"]
                 ;;;
                 
                 
                 ;;; this is new
                 [org.clojure/tools.cli "0.4.1"]
                 [reply/reply "0.4.3"]
                 ;;;;;;;
                 
                 
                 ]
  
  
  :profiles
    {:dev {:source-paths ["dev" "src" "test"]
           :dependencies [ [org.clojure/tools.namespace "0.2.10"]   ]
           :plugins [  [lein-ancient "0.6.15"]  ]    }
   ;:uberjar {:aot :all} 
      }
  
  
  
  
  :source-paths ["src/clj/"]
  :source-path "src/clj/"
  :java-source-paths ["src/jvm/"] ;leiningen 2 compat.
  :java-source-path "src/jvm/" ;leiningen 1.x compat.
  ;:project-init (require 'clojure.pprint) 
  :repl-options {:init-ns zinc.core.snuser
                  :print clojure.core/println} ; :print clojure.pprint/pprint
                  :timeout 250000
                 
  :jvm-opts ["-server"] 
  ;:main zinc.core.snuser
  
  
  
  ;:local-repo  "../../bobtailbot/local-m2"
  
  
  )
