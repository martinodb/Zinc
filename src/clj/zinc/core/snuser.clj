;(set! *warn-on-reflection* true)

(ns zinc.core.snuser
  (:require [zinc.core.contexts :as ct]
            [zinc.core.caseframes :as cf]
            [zinc.core.relations :as slot]
            [zinc.core :as zinc]
            [zinc.core.build :as build]
            [zinc.snip :as snip]
            [zinc.gui :as gui]
            [zinc.utils.ontology :as onto-tools]
            [clojure.tools.cli :refer [parse-opts]]
            [reply.main])
  (:use clojure.stacktrace)
  (:refer-clojure :exclude [+ - * / < <= > >= == not= assert find load exit quit])
  (:use [clojure.pprint :only (cl-format)]
        [clojure.core.memoize :only (memo-clear!)]
        [clojure.walk]
        [zinc.core.caseframes :only (list-caseframes sameFrame description)]
        [zinc.demo :only (demo)]
        [clojure.set :only (union difference)]
        [zinc.core.relations :only (list-slots)]
        [zinc.core.contexts :only (currentContext defineContext listContexts setCurrentContext remove-from-context)]
        
        ;[zinc.core.build :only (find *PRECISION* defrule unassert rewrite-propositional-expr)]
        [zinc.core.build :only (find *PRECISION* defrule rewrite-propositional-expr)] ; remove 'unassert'.
        
        [zinc.core :only (showTypes list-types semantic-type-of)]
        [zinc.core.printer :only (writeKBToTextFile)]
        [zinc.snip :only (definePath pathsfrom cancel-infer-of cancel-infer-from cancel-focused-infer adopt unadopt attach-primaction ig-debug-all)]
        [zinc.core.arithmetic]
        [zinc.util]
        [zinc.debug :only (debug set-debug-nodes set-debug-features)])
  (:import [edu.buffalo.zinc.util CountingLatch]))

(declare askif askifnot defineTerm find-term             clearkb          )

(defn adopt-rule
  "Adopts the rule with the symbol rule-name as its name."
  [rule-name]
  (let [rules (filter #(isa? (zinc/syntactic-type-of %) :zinc.core/CARule) (vals @zinc/TERMS))
        rule (filter #(= rule-name (:name (ffirst (@zinc/down-cableset %)))) rules)]
    (if (first rule)
      (when-let [taskid (adopt (first rule))]
        (.await ^CountingLatch (@snip/infer-status taskid)))
      (error "Rule " rule-name " does not exist."))))

(defn adopt-rules 
  "Takes a list of symbolic rule names to be adopted in order, one after the other. 
   Rows may take the form of a single rule name, or a vector of rule names. A vector
   of rule names will be adopted simultaneously."
  [order]
  (doseq [row order]
    (if (vector? row)
      (let [tasks (doall (map #(future (adopt-rule %)) row))]
        (doall (map deref tasks)))
      (adopt-rule row))))





;;;; martinodb.

(defn unassert [expr & {:keys [precision]}]
  (binding [*PRECISION* (or precision *PRECISION*)]
    (rewrite-propositional-expr expr)
    (build/unassert expr (currentContext))))

;;;;;





(defn assert [expr & {:keys [precision]}]
  (binding [*PRECISION* (or precision *PRECISION*)]
    (rewrite-propositional-expr expr)
    (build/assert expr (currentContext))))

(defn assert! [expr & {:keys [precision]}]
  (binding [*PRECISION* (or precision *PRECISION*)]
    (rewrite-propositional-expr expr)
    (let [term (build/assert expr (currentContext))]
      (snip/forward-infer term)
      term)))

(defn assertAll [exprs & {:keys [precision]}]
  (binding [*PRECISION* (or precision *PRECISION*)]
    (doseq [expr exprs] (assert expr))))

(defn ask
  "Returns a set of instances of the term pattern exprpat or its negation
        that are derivable in the current context;
        or the empty set if there are none."
  [exprpat]
  (if (some build/synvariable? (flatten exprpat))
    (snip/askwh-instances exprpat (ct/currentContext))
    (setOr				
      (askif exprpat)
      (askifnot exprpat))))

(defn askif
  "Returns a set of instances of the term pattern exprpat
         that are derivable in the current context;
         or the empty set if there are none."
  [exprpat]
  (rewrite-propositional-expr exprpat)
  (snip/askif (build/variable-parse-and-build exprpat :Proposition #{})  
              (currentContext) 
              nil))

(defn askifnot
  "Returns a set of instances of the negation of the term pattern exprpat
         that are derivable in the current context;
         or the empty set if there are none."
  [exprpat]
  (rewrite-propositional-expr (list 'not exprpat))
  (snip/askif (build/variable-parse-and-build (list 'not exprpat) :Proposition #{})
              (currentContext)
              nil))

(defn askwh
  ""
  [exprpat]
  (snip/askwh exprpat (currentContext)))

(defn allTerms [& {:keys [test] :or {test identity}}]
  "Returns a set of all the terms in the knowledge base."
  (set (filter test (vals @zinc/TERMS))))

(defmacro defineSlot
  [name & args]
  (let [kws (take-nth 2 args)
        vals (take-nth 2 (rest args))
        qtvals (map (fn [v] `'~v) vals)
        call (interleave kws qtvals)
        zm (zipmap kws qtvals)]
    `(let [slot# (slot/define-slot '~name ~@call)]
       (if (get ~zm :path)
         (definePath '~name (get ~zm :path)))
       slot#)))

(defn defineCaseframe
  [type frame & {:keys [docstring fsymbols] :or {docstring ""}}]
    ;(println frame (first frame) (clojure.core/type (first frame)) (clojure.core/type (first (first frame))))
    (cf/define-caseframe
      type
      (cond
        (and (seq? frame) (symbol? (first frame)))   (seq frame)
        (and (seq? (first frame)) (= (ffirst frame) 'quote))  (seq (rest frame))
        :else (error "The frame, ~S, must be a list of slot names or ~
                                    a list containing a quoted atomic constant followed by slot names."))
      :docstring docstring
      :print-pattern frame
      :fsymbols fsymbols))

(defn defineTerm
  "Finds or builds the given term,
    assures that it is over the given semantic type,
    and returns the term."
  [term & semtype]
  (cond
    (and (seq? term) (or (= (first term) 'some)
                         (= (first term) 'every)))
    (build/build-variable term)
    (seq? term)
    (do 
      (rewrite-propositional-expr term)
      (build/variable-parse-and-build term :Propositional #{}))
    :else
    (build/build term (or (first semtype) :Entity) {} #{})))

(defmacro defineType
  "Semantic types are stored internally as keywords, so they are converted to keywords first if 
   they are not already. Thisconversion maintains some backward compatibility with SNePS 3."
  [newtype supers & docstring]
  `(let [newtypekey# (keyword '~newtype)
         newsuperkeys# (map keyword '~supers)]
     (dosync (build/define-type newtypekey# newsuperkeys#))
     (println newtypekey# " defined as a subtype of " newsuperkeys#)))

(defmacro describe-terms
  "Prints a description of all the given terms."
  [& wftnames]
  `(doseq [tname# '~wftnames]
     (println (description (find-term tname#)))))

(defn find-term
  [term]
  ;; Cast to a string before a symbol since numbers cannot be converted directly to symbols.
  (zinc/get-term (symbol (str term))))

(defn list-focused-inference-tasks
  []
  (let [fw-tasks (apply union (vals @zinc/future-fw-infer))
        bw-tasks (apply union (map #(deref (:future-bw-infer %)) (vals @zinc/TERMS)))]
    (when (seq bw-tasks) (println "Attempting to derive:"))
    (doseq [t bw-tasks]
      (println t))
    (when (seq fw-tasks) (println "Deriving from:"))
    (doseq [t fw-tasks]
      (println t))))

(defn list-variables
  "Prints the variable nodes. First arbitraries and then indefinites. If the 
   types keyword is not nil, then it prints the types of each term."
  [& {:keys [types]}]
  (doseq [arb @zinc/ARBITRARIES]
    (cl-format true "~:[~*~;<~S> ~]~S~%" 
               types (type arb) arb))
  (doseq [ind @zinc/INDEFINITES]
    (cl-format true "~:[~*~;<~S> ~]~S~%" 
               types (type ind) ind))
  (doseq [qvar @zinc/QVARS]
    (cl-format true "~:[~*~;<~S> ~]~S~%" 
               types (type qvar) qvar)))

(defn list-terms
  ""
  ;; First print atomic terms;
  ;; Then arbitrary nodes
  ;; Then indefinite nodes
  ;; Then qvar nodes
  ;; Then print molecular terms;
  [& {:keys [asserted types originsets properties ontology]}]
  (let [terms (vals @zinc/TERMS)
        atoms (sort-by :name (filter #(= (:type %) :zinc.core/Atom) terms))
        arbs (sort-by :name (filter zinc/arbitraryTerm? terms))
        inds (sort-by :name (filter zinc/indefiniteTerm? terms))
        qvars (sort-by :name (filter zinc/queryTerm? terms))
        mols (sort-by :name (filter zinc/molecularTerm? terms))
        print-term (fn [x] 
                     (when types (print (zinc/syntactic-type-of x) "-" (zinc/semantic-type-of x) " "))
                     (if (and properties (@zinc/property-map x)) (print (@zinc/property-map x) " ") #{})
                     (print x)
                     (when originsets (print " " (@zinc/support x)))
                     (println))]
    (doseq [x (concat atoms arbs inds qvars mols)]
      (let [asserted-in-ct (ct/asserted? x (ct/currentContext))
            ontological-term (ct/ontology-term? x)]
        (cond 
          (and (not asserted) (not ontology) (not ontological-term)) (print-term x)
          (and asserted asserted-in-ct ontology) (print-term x)
          (and asserted asserted-in-ct (not ontology) (not ontological-term)) (print-term x)
          (and (not asserted) ontology) (print-term x))))))

(defn listkb
  "Prints the current context and all propositions asserted in it."
  []
  (println (:name (ct/currentContext)))
  (doseq [i (str (:name (ct/currentContext)))]
    (print "-"))
  (println)
  (list-terms :asserted true))

(defn krnovice
  [b]
  (dosync (ref-set build/KRNovice b)))

(defn goaltrace
  []
  (reset! snip/goaltrace true))

(defn nogoaltrace
  []
  (reset! snip/goaltrace false))

;(defn startGUI
  ;([] (gui/startGUI))
  ;([termset] (gui/startGUI termset)))

(defn load
  [fname]
  (load-file fname))

(defn quit
  []
  (shutdown-agents)
  (zinc.snip/shutdownExecutor)
  (System/exit 0))

(defn exit [] (quit))

(clojure.core/load "/zinc/core/initialize")
(clojure.core/load "/zinc/test/benchmark")
(clojure.core/load "/zinc/test/mapper_benchmark")

(def cli-options
  ;; An option with a required argument
  [["-c" "--cli"] ;; Use CLI (if nil, use GUI)
   ["-h" "--help"]]) ;; Help



;;;; martinodb: I had to add this bc -main is commented out.
(clearkb true)
;;;;;




;(defn -main [& args]
  ;(let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;(when (:help options)
      ;(println summary)
      ;(System/exit 0))
    ;(clearkb true)
    ;(if (:cli options)
      ;(reply.main/launch {:custom-eval '(in-ns 'zinc.core.snuser)})
      ;(gui/startGUI))))

