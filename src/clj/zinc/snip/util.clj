(ns zinc.snip.util
  (:require [zinc.core :as zinc]
            [zinc.core.contexts :as ct]
            [zinc.core.build :as build]
            [clojure.set :as set]))

(defn variables-in
  [term]
  (loop [dcs #{term}
         vars #{}]
    (if (seq dcs)
      (if (build/variable? (first dcs))
        (recur (set (rest dcs)) (conj vars (first dcs)))
        (recur (apply set/union (set (rest dcs)) (:down-cableset (first dcs))) vars))
      vars)))