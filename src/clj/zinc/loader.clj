(ns zinc.loader
  (:require [zinc.core.printer]
            [zinc.gui])
  (:use [zinc.util]))

;; Load the util namespace
(load "util")
;; Load the namespace contexts
(load "core/contexts")
;; Load the namespace sneps3
;(load "zinc/core_syntactic_types")
;(load "zinc/core_semantic_types")
;; Load the 'relations' namespace
(load "core/relations")
;; Load the 'find' namespace
;(load "find")
;; Load the 'caseframes' namespace
(load "core/caseframes")
;; Load the sneps3-printer namespace
(load "core/printer")
;; Load the contexts namespace
(load "core/contexts")
;; Load the build namespace
(load "core/build")
;; Load the arithmetic namespace.
(load "core/arithmetic")
;; Load the snip namespace.
(load "snip/snip")
;; Load the snuser namespace.
(load "core/snuser")
;; Initialize the system.
;(load "initialize")

(zinc.core.snuser/clearkb true)


(defn loadsneps3 []
  (load "/sneps")
  (set! *print-length* 40)
  (set! *print-level* 4)
  (in-ns 'snuser)
  ;(clojure.main/repl :print println)
)

(defn loadsneps3gui []
  ;(load "/sneps")
  (set! *print-length* 40)
  (set! *print-level* 4)
  (gui/startGUI))

(defn loadsneps3-debug []
  ;(load "sneps")
  (set! *print-length* 40)
  (set! *print-level* 4)
  (clojure.main/repl :print clojure.pprint/pprint))

(defn loadsneps3-newprint []
  ;(load "sneps")
  (set! *print-length* 40)
  (set! *print-level* 4)
  (clojure.main/repl :print sneps3-printer/sneps-printer))

(defn -main [& args]
  (loadsneps3gui))