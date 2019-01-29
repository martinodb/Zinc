(ns zinc.core
  (:require [clojure.pprint]
            [clojure.set]
            [clojure.string :as st])
  (:use [zinc.util]))

;; Load the rest of the zinc.core namespace.
(load "core_syntactic_types")
(load "core_semantic_types")