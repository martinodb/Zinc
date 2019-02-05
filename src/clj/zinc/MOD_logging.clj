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
(defmacro wdm "returns a data map with a key for the results and another for the output string"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         {:result r#
          :strn    (str s#)}))))



(defmacro wtim "returns the result, logs the output string as info. Uses Timbre logging."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         (do (timbre/info (str s#))
             r#) ))))


(defmacro wlog "returns the result, logs the output string as info. Uses clojure.tools.logging."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         (do (log/info (str s#))
             r#) ))))


;;;;;;;;;


(defmacro wdm2 "Like wdm, but takes only one sexpr, without the parens, so there's no nesting"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~body]
         {:result r#
          :strn    (str s#)}))))



(defmacro wtim2 "Like wtim, but takes only one sexpr, without the parens, so there's no nesting."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~body]
         (do (timbre/info (str s#))
             r#) ))))


(defmacro wlog2 "Like wlog, but takes only one sexpr, without the parens, so there's no nesting."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [r# ~body]
         (do (log/info (str s#))
             r#) ))))

;;;;;;;;;
;;;;;;;;;



(defn println-wtim "println replacement. Returns the result, logs the output string as info. Uses Timbre logging."
[& more] (->> (apply println more)(wtim))  )

(defn println-wlog "println replacement. Returns the result, logs the output string as info. Uses clojure.tools.logging."
[& more] (->> (apply println more)(wlog))  )


(defn cl-format-wtim "cl-format replacement. Returns the result, logs the output string as info. Uses Timbre logging."
   [writer format-in & args]
  (let [args2  (into [writer format-in] args)]
    (->> (apply cl-format args2) (wtim)  )  )  )

(defn cl-format-wlog "cl-format replacement. Returns the result, logs the output string as info. Uses clojure.tools.logging."
   [writer format-in & args]
  (let [args2  (into [writer format-in] args)]
    (->> (apply cl-format args2) (wlog)  )  )  )





;;;;;;;;;;





;;;; pick one.
;(def println-info    println-wlog)
(def println-info    println-wtim)
;;;;;;;;;;;;;;;;;;;;;;;


;;;; pick one.
;(def cl-format-info    cl-format-wlog)
(def cl-format-info cl-format-wtim)
;;;;;;;;;;;;;;;;;;;;;;



