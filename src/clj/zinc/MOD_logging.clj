;;; This ns was added by martinodb

(ns zinc.MOD-logging
 (:require
          [clojure.tools.logging :as log]
          
          ;; Timbre
          [taoensso.timbre :as timbre   :refer [log  trace  debug  info  warn  error  fatal  report logf tracef debugf infof warnf errorf fatalf reportf  spy get-env]]
          [taoensso.timbre.appenders.core :as appenders]
          ;;;;;
          
          [clojure.pprint :as pp :refer [cl-format]]
           ))





;; https://clojuredocs.org/clojure.core/with-out-str
(defmacro with-out-str-dm "returns a data map with a key for the results and another for the output string"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         {:result r#
          :strn    (str s#)}))))



(defmacro wrnlog-info-timbre "returns the result, logs the output string as info. Uses Timbre logging."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         (do (timbre/info (str s#))
             r#) ))))


(defmacro wrnlog-info-log "returns the result, logs the output string as info. Uses clojure.tools.logging."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         (do (log/info (str s#))
             r#) ))))

;;;;

(defn println-wrnlog-info-timbre "println replacement. Returns the result, logs the output string as info. Uses Timbre logging."
[& more] (->> (apply println more)(wrnlog-info-timbre))  )

(defn println-wrnlog-info-log "println replacement. Returns the result, logs the output string as info. Uses clojure.tools.logging."
[& more] (->> (apply println more)(wrnlog-info-log))  )


(defn cl-format-wrnlog-info-timbre "cl-format replacement. Returns the result, logs the output string as info. Uses Timbre logging."
   [writer format-in & args]
  (let [args2  (into [writer format-in] args)]
    (->> (apply cl-format args2) (wrnlog-info-timbre)  )  )  )

(defn cl-format-wrnlog-info-log "cl-format replacement. Returns the result, logs the output string as info. Uses clojure.tools.logging."
   [writer format-in & args]
  (let [args2  (into [writer format-in] args)]
    (->> (apply cl-format args2) (wrnlog-info-log)  )  )  )





;;;; pick one.
;(def println-info    println-wrnlog-info-log)
(def println-info    println-wrnlog-info-timbre)
;;;;;;;;;;;;;;;;;;;;;;;


;;;; pick one.
;(def cl-format-info    cl-format-wrnlog-info-log)
(def cl-format-info cl-format-wrnlog-info-timbre)
;;;;;;;;;;;;;;;;;;;;;;



