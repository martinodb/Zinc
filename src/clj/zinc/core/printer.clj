;;; sneps3_printer.clj
;;; A printer for SNePS 3 terms.
;;; Author: Daniel R. Schlegel
;;; Modified: 6/10/2014

(ns zinc.core.printer
  (:require [clojure.string :as str]
            [zinc.core.contexts :as ct]
            [zinc.core :as zinc]
            [zinc.core.caseframes :as cf]
            [zinc.core.relations :as slot])
  (:use [zinc.core :only (type-of)]
        [clojure.pprint :only (cl-format pprint)]
        [zinc.util]
        [clojure.java.io]))

(def ^{:dynamic true} PRINTED-VARIABLES (hash-set))
(def ^{:dynamic true} PRINTED-VARIABLE-LABELS (hash-map))

(declare print-term print-set)

(defn print-atom
  [term]
  (if (str/includes? (:name term) " ")
    (str "\"" (:name term) "\"")
    (:name term)))

(defn wft-string
  [term]
  (str (:name term)
      (if (or (zinc/subtypep (zinc/semantic-type-of term) :Proposition)
              (zinc/subtypep (zinc/semantic-type-of term) :Policy))
        (if (ct/asserted? term (ct/currentContext)) "!" "?"))":"))

(defn args-str
  [args]
  (map #(print-term %) args))

(defn print-negation
  [args]
  (print-str (list* (if (= (count args) 1) 'not 'nor) (args-str args))))

(defn print-negationbyfailure
  [args]
  (print-str (list* (if (= (count args) 1) 'thnot 'thnor) (args-str args))))

(defn print-param2op
  [fcn min max args]
  (print-str (list* fcn (list min max) (args-str args))))

(defn print-nary
  [fcn args]
  (print-str (list* fcn (args-str args))))

(defn print-molecular
  [cf cs]
  (str "(" 
         (apply str 
                (interpose " " 
                           (if (cf/hasOneArgumentSlot cf)
                             (let [fsym (first (:print-pattern cf))]
                               (if (and (seq? fsym)
                                        (= 'quote (first fsym)))
                                 (cons (second fsym) (if (set? (first cs)) 
                                                       (map #(print-term %) (first cs)) 
                                                       (list (print-term (first cs)))))
                                 (cons (print-term (first cs)) (if (set? (second cs)) 
                                                                 (map #(print-term %) (second cs)) 
                                                                 (list (print-term (second cs)))))))
                             (for [p (:print-pattern cf)]
                               (cond
                                 (and (seq? p) (= (first p) 'quote))
                                 (second p)
                                 (symbol? p)
                                 (print-term (nth cs (first (positions #(= % p) (map #(:name %) (:slots cf))))))
                                 :else 
                                 (error "Bad pattern part "p" in the pattern "(:print-pattern cf)"."))))))
         ")"))

(defn get-var-label
  [term]
  (or (PRINTED-VARIABLE-LABELS term)
      (let [var-label (if ((set (vals PRINTED-VARIABLE-LABELS)) (:var-label term))
                                  (str "x" (count PRINTED-VARIABLE-LABELS))
                                  (:var-label term))]
        (set! PRINTED-VARIABLE-LABELS (assoc PRINTED-VARIABLE-LABELS term var-label))
        var-label)))

(defn print-ind-deps
  [ind]
  (let [deps (@zinc/dependencies ind)
        labels (map get-var-label deps)]
    (apply str (interpose " " labels))))

(defn print-unnamed-variable-term
  [term]
  (str
    (cond
      (PRINTED-VARIABLES term) (PRINTED-VARIABLE-LABELS term)
      :true
      (let [var-label (get-var-label term)]
        (set! PRINTED-VARIABLES (conj PRINTED-VARIABLES term))
        (str 
          (condp = (type-of term)
            :zinc.core/Arbitrary (str "(every " var-label " ")
            :zinc.core/Indefinite (str "(some " var-label " (" (print-ind-deps term) ") ")
            :zinc.core/QueryVariable (str "(" var-label " "))
          (print-set (@zinc/restriction-set term) false)
          (when (seq @(:not-same-as term))
            (str " (notSame " var-label " " (print-set @(:not-same-as term) false) ")"))
          ")")))))

(defn print-closure
  [term]
  (print-str (list 'close (map get-var-label (:closed-vars term)) (print-term (first (@zinc/down-cableset term))))))

(defn print-carule
  [term]
  (let [dcs (cf/dcsRelationTermsetMap term)
        rulename (first (dcs (slot/find-slot 'rulename)))
        condition (map print-term (dcs (slot/find-slot 'condition)))
        action @(:print-forms term)
        subrules (map print-term (dcs (slot/find-slot 'subrule)))]
    (print-str (list (if (str/includes? rulename "subrule")
                       :subrule
                       (print-str 'defrule rulename))
                     (str/join " " condition)
                     '=> 
                     action
                     (str/join " " subrules)))))

(defn print-unnamed-molecular-term
  [term]
  ;(println "Printing term: " term)
  (condp = (type-of term)
    :zinc.core/CARule
      (print-carule term)
    :zinc.core/Closure
      (print-closure term)
    :zinc.core/Negation
      (print-negation (first (@zinc/down-cableset term)))
    :zinc.core/Negationbyfailure
      (print-negationbyfailure (first (@zinc/down-cableset term)))
    :zinc.core/Conjunction
      (print-nary 'and (first (@zinc/down-cableset term)))
    :zinc.core/Disjunction
      (print-nary 'or (first (@zinc/down-cableset term)))
    :zinc.core/Equivalence
      (print-nary 'iff (first (@zinc/down-cableset term)))
    :zinc.core/Xor
      (print-nary 'xor (first (@zinc/down-cableset term)))
    :zinc.core/Nand
      (print-nary 'nand (first (@zinc/down-cableset term)))
    :zinc.core/Andor
      (print-param2op 'andor (:min term) (:max term) (first (@zinc/down-cableset term)))
    :zinc.core/Thresh
      (print-param2op 'thresh (:min term) (:max term) (first (@zinc/down-cableset term)))
    :zinc.core/Implication
      (print-str (list 'if (print-term (first (@zinc/down-cableset term))) (print-term (second (@zinc/down-cableset term)))))
    :zinc.core/Numericalentailment
      (print-str (list (symbol (str "=" (if (= (:min term) 1) 'v (:min term)) ">"))
                       (print-term (first (@zinc/down-cableset term))) (print-term (second (@zinc/down-cableset term)))))
    :zinc.core/Arbitrary
      (print-unnamed-variable-term term)
    :zinc.core/Indefinite
      (print-unnamed-variable-term term)
    :zinc.core/QueryVariable
      (print-unnamed-variable-term term)
    (print-molecular (@zinc/caseframe term) (@zinc/down-cableset term))
    ))

(defn sneps-printer
  [x]
  (condp = (type x)
    zinc.core.Atom (print-atom x *out*)
    zinc.core.Categorization (print-molecular x *out*)
    (pprint x *out*)))

;(defn print-named-molecular-term
;  "Prints to the stream
;        the molecular term preceded by its wft name
;        and an indication of its assert status."
;  [term stream]
;  (cl-format stream "~@<~W~:[~*~;~:[?~;!~]~]: ~W~:>"
;	  (:name term)
;	  (= (zinc/semantic-type-of term) :Proposition)
;	  (ct/asserted? term (ct/currentContext)) term))

(defn print-named-variable-term
  [term]
  (str (:name term) ": " (print-unnamed-variable-term term)))


;(defmethod print-method zinc.core.Categorization [o w]
;  (print-named-molecular-term o w))

(defn print-set
  [slotset show-setof?]
  (if (= (count slotset) 1)
    (print-term (first slotset))
    (if show-setof?
      (str "#{" (apply str (interpose " " (for [n slotset] (print-term n)))) "}")
      (str (apply str (interpose " " (for [n slotset] (print-term n))))))))

(defn print-term
  [term]
  ;(println "Term: " term)
  (condp = (type-of term)
    clojure.lang.PersistentHashSet (print-set term true)
    :zinc.core/Atom (print-atom term)
    (print-unnamed-molecular-term term)))

(defn term-printer
  [term]
  (binding [PRINTED-VARIABLES (hash-set)
            PRINTED-VARIABLE-LABELS (hash-map)]
  (cond
    ;; Variable
    (isa? (zinc.core/syntactic-type-of term) :zinc.core/Variable)
    (print-str (print-named-variable-term term))
    ;; Molecular
    (isa? (zinc.core/syntactic-type-of term) :zinc.core/Molecular)
    (print-str (wft-string term) (print-term term))
    ;; Term Set
    (set? term)
    (print-set term true)
    :else
    (str (print-atom term)
      (if
        (and (= (zinc.core/semantic-type-of term) :Proposition)
	     (ct/asserted? term (ct/currentContext)))
        "!" "")))))

(defn sneps-printer
  [object]
  (if (isa? (zinc.core/syntactic-type-of object) :zinc.core/Term)
    (pprint (term-printer object))
    (pprint object)))

(defn sneps-printer-str
  [object]
  (if (isa? (zinc.core/syntactic-type-of object) :zinc.core/Term)
    (with-out-str (pprint (term-printer object)))
    (with-out-str (pprint object))))

(defn sexpr-printer
  [object]
  (cond
    (seq? object)
    (for [o object]
      (if (:name o)
        (:name o)
        (sexpr-printer o)))
    (set? object)
    (set (for [o object]
      (if (:name o)
        (:name o)
        (sexpr-printer o))))
    :else object))

(defn unif-printer
  [ulist]
  ;(println ulist)
  (for [m ulist]
    (into {} (map
      (fn [[k v]]
        [
         (if (isa? (zinc.core/syntactic-type-of k) :zinc.core/Variable)
           (print-atom k)
           (sexpr-printer k))
         (if (isa? (zinc.core/syntactic-type-of v) :zinc.core/Variable)
           (print-atom v)
           (sexpr-printer v))
         ]) m))))

;;; Print-method functions
(defmethod print-method zinc.core.Term [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Atom [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Base [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Variable [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Arbitrary [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Indefinite [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.QueryVariable [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Molecular [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Closure [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Carule [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Param2op [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Andor [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Disjunction [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Xor [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Nand [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Thresh [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Equivalence [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Conjunction [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Negation [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Negationbyfailure [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Numericalentailment [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Orentailment [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Implication [o w]
  (.write ^java.io.Writer w (str (term-printer o))))

(defmethod print-method zinc.core.Categorization [o w]
  (.write ^java.io.Writer w (str (term-printer o))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Writing CSNePS KBs to Files ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn writeKBToTextFile 
  "Writes the KB to the given text file,
   so that when that file is loaded,
   all the propositions asserted in the current KB
   will be asserted in the new KB.
   If the headerfile is included,
      a load of that file will be written before any of the asserts."
  [file & headerfile]
  (with-open [w ^java.io.Writer (writer file)]
    (.write w ";;; CSNePS KB\n")
    (.write w ";;; =========\n")
    (.write w (str ";;; " (.toString (new java.util.Date)) "\n"))
    (when headerfile 
      (.write ^java.io.Writer w "(clojure.lang.Compiler/loadFile " (first headerfile) ")\n"))
    (.write w ";;; Assumes that all required Contexts, Types, Slots, and Caseframes have now been loaded.\n(in-ns 'zinc.core.snuser)\n")
    (doseq [term (vals @zinc/TERMS)]
      (when (ct/asserted? term (ct/currentContext))
        (.write w  "(zinc.core.build/assert '")
        (if (= (:type term) :zinc.core/Atom)
          (.write w (str (print-atom term)))
          (binding [PRINTED-VARIABLES (hash-set)
                    PRINTED-VARIABLE-LABELS (hash-map)]
            (.write w (str (print-unnamed-molecular-term term)))))
        (.write w (str " 'DefaultCT)\n"))))))
      

  
  
  


      