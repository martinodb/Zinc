(ns zinc.test.unification
  (:use [zinc.core.build])
  (:require [zinc.core.snuser]))

(defn basicunif-test []
  (addTermToUnificationTree (zinc.core.snuser/assert '(Isa (every x) Animal)))
  (getUnifiers (zinc.core.snuser/defineTerm '(Isa Glacier Animal))))

(defn fullunif-test []
  (zinc.core.snuser/clearkb true)
  (zinc.core.snuser/defineSlot entity :type Entity :docstring "General slot for holding entities.")
  (zinc.core.snuser/defineSlot entity1 :type Entity :docstring "General slot for holding entities.")
  (zinc.core.snuser/defineSlot entity2 :type Entity :docstring "General slot for holding entities.")
  (zinc.core.snuser/defineCaseframe 'Proposition '('SameSpecies entity entity1 entity2))
  (zinc.core.snuser/defineCaseframe 'Proposition '('caregiver entity1))
  (zinc.core.snuser/defineCaseframe 'Proposition '('friend entity2))
  (addTermToUnificationTree (zinc.core.snuser/assert '(SameSpecies (caregiver (every x)) (friend (caregiver (every w))) w)))
  (getUnifiers (zinc.core.snuser/defineTerm '(SameSpecies (every x) (friend x) Alex))))