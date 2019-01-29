(ns zinc.core.contexts
  (:require [zinc.core :as zinc]
            [clojure.string :as str]
            [clojure.set :as set])
  (:use [zinc.util]))

(defvar CONTEXTS (ref (hash-map))
  "A map from context name to context.")

(defvar ^:dynamic *CurrentContext* (ref nil)
  "The current context.")

;;; "A CSNePS Context"
(defrecord Context [
        ^clojure.lang.Symbol name
        ^String docstring
        ^clojure.lang.PersistentList parents
        ^clojure.lang.PersistentHashSet hyps
        ^java.lang.Boolean kinconsistent])

(defmethod print-method zinc.core.contexts.Context [o w]
  (.write ^java.io.Writer w (str (:name o) " - " (:docstring o)
                                 "\n\tParents: " (str/join ", " (map :name (:parents o)))
                                 "\n\tConsistent?: " (not (:kinconsistent o)))))

(defn find-context
  "If ctx is a context, returns it.
     If ctx is a symbol, returns the context named ctx
      or nil if there isn't any.
     If ctx is nil, just return nil."
  [ctx]
  (typecase ctx
    Context ctx
    clojure.lang.Symbol (get @CONTEXTS ctx)
    nil nil))

(defn setCurrentContext
  "If ctx is a context name,
         makes the context named ctx the current context.
     If ctx is a context, makes it the current context.
     Else throws an exception."
  [ctx]
  (assert (or (instance? Context ctx)
              (and (symbol? ctx) (find-context ctx))))
  (typecase ctx
    clojure.lang.Symbol (dosync (ref-set *CurrentContext* (find-context ctx)))
    Context (dosync (ref-set *CurrentContext* ctx))))

;;PORTNOTE: I changed hyps to init as (hash-set) and not nil.
(defn defineContext
  ""
  [name & {:keys [docstring parents hyps]
           :or {docstring "", parents '(BaseCT), hyps (hash-set)}}]
  (assert (symbol? name))
  (assert (string? docstring))
  (assert (or (nil? parents) (cl-seqable? parents)))
  (assert (cl-seqable? hyps) (set? hyps))
  (if (find-context name)
    (println "A context named" name "already exists.")
    (dosync (alter CONTEXTS assoc name (Context. name docstring 
                                                 (doall (map #(find-context %) parents)) 
                                                 (ref (set hyps))
                                                 false))))
  (find-context name))

(defn currentContext
  "Returns the current context."
  []
  @*CurrentContext*)

(defn listContexts
  "Prints a list of all the contexts."
  []
  (doseq [n (keys @CONTEXTS)]
    (println n)))

(defn remove-from-context
  "Removes the term from the context (ct) hyps."
  [term ct]
  (let [term (or (:name term) term)]
    (dosync (alter (:hyps ct) disj term))))

(defn hypothesize
  [term ct]
  (let [term (or (:name term) term)]
    (dosync (alter (:hyps ct) conj term))))

(defn hyps
  "Returns the full set of hyps of ct, both those local to
   ct, and those of its parents."
  [ct]
  (loop [cts (list ct)
         hyps #{}]
    (if (empty? cts)
      hyps
      (recur (apply concat (map #(:parents %) cts))
             (set/union hyps (apply set/union (map #(deref (:hyps %)) cts)))))))

(defn asserted?
  "If p has an origin set which is a subset of the hyps of the
   context ct, then it is asserted. Local key indicates to only
   look in the current context, and not parents."
  [p ct & {:keys [local]}]
  (let [context (find-context ct)]
    (when context 
      (if (@(:hyps context) (:name p)) 
        context
        (let [cthyps (if local 
                       @(:hyps context) 
                       (hyps context))]
          (cond
            (cthyps (:name p)) 
            context
            (some 
              #(set/subset? (second %) cthyps)
              (filter #(not (= (first %) 'hyp)) (@zinc/support p)))
            context))))))

(defn ontology-term?
  "Returns true if p is an ontology term. That is either: 
   1) p is asserted in OntologyCT, or 
   2) p is named the same as a semantic type, or
   3) p is an arbitrary with only a single restriction of the form (Isa x <SemanticType>)"
  [p]
  (or
    (asserted? p (find-context 'OntologyCT) :local true)
    (zinc/semtype? (keyword (:name p)))
    (and
      (zinc/arbitraryTerm? p)
      (= 1 (count (@zinc/restriction-set p)))
      (let [res (first (@zinc/restriction-set p))
            cf (second (first (:print-pattern (@zinc/caseframe res))))
            arg2 (when (= cf 'Isa) (second (@zinc/down-cableset res)))]
        (when arg2
          (every? #(zinc/semtype? (keyword (:name %))) arg2))))))
            
      