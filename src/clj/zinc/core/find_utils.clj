(ns zinc.core.find-utils
  (:use [zinc.core]
        [zinc.util])
  (:require [zinc.core.relations :as slot]
            [clojure.test :refer [is]]))

(defn findto
  "Returns the set of nodes to which a slot, r, goes from n, including
    possibly the empty set."
  [n r]
  (let [rel (if (symbol? r)
              (slot/find-slot r)
              r)]
    (when (isa? (type-of n) :zinc.core/Molecular)
      (let [pos (first (positions #{rel} (:slots (@caseframe n))))]
        (if pos
          (nth (seq (@down-cableset n)) pos)
          #{})))))

(defn findfrom
  "Returns the set of nodes
        from which a slot r, or a slot named r, goes to m."
  [m r]
  {:pre [(is (term? m) "m is not a term.")]}
  (let [res (get (@up-cablesetw m) (if (= (type r) zinc.core.relations.Slot) r (slot/find-slot r)))]
    (if res @res (hash-set))))