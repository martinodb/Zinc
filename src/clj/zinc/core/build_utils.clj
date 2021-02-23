(in-ns 'zinc.core.build)

(defn ignore-variable? [sym] (= '_ sym))

(def variable?
  (fn [term]
    (variableTerm? term)))

;(def variable? #(or (= (type-of %) :zinc.core/Arbitrary) (= (type-of %) :zinc.core/QueryVariable)))

(def synvariable? #(or (ignore-variable? %)
                      (and (symbol? %) (re-matches #"^\?.*" (name %)))))

(def syntype-fsym-map {:zinc.core/Negation 'not,
                       :zinc.core/Negationbyfailure 'thnot,
                       :zinc.core/Conjunction 'and,
                       :zinc.core/Disjunction 'or,
                       :zinc.core/Equivalence 'iff,
                       :zinc.core/Xor 'xor,
                       :zinc.core/Nand 'nand,
                       :zinc.core/Andor 'andor,
                       :zinc.core/Thresh 'thresh,
                       :zinc.core/Implication 'if})

(defn term-predicate
  [term]
  (or
    ((type-of term) syntype-fsym-map)
    (let [p (:print-pattern (@caseframe term))]
      (if (and (seq? (first p)) (= (first (first p)) 'quote))
        (second (first p))
        (:name (first (first (@down-cableset term))))))))

(defn term-walk
  [inner outer termpart & {:keys [ignore-type with-restrictions]}]
  (cond
    (molecularTerm? termpart) (outer (build 
                                       (if-let [fsym (or ((type-of termpart) syntype-fsym-map)
                                                         (let [p (:print-pattern (@caseframe termpart))]
                                                           (when (and (seq? (first p)) (= (first (first p)) 'quote))
                                                             (second (first p)))))]
                                         (conj (doall (map inner (@down-cableset termpart))) fsym)
                                         (doall (map inner (@down-cableset termpart))))
                                       (if ignore-type :Entity (zinc.core/semantic-type-of termpart))
                                       {}
                                       #{}))
    (atomicTerm? termpart) (outer termpart)
    (set? termpart) (set (doall (map inner termpart)))
    :else (error (str "Term contains unknown parts (" termpart ")"))))

(defn term-recur
  [inner outer termpart]
  (cond
    (molecularTerm? termpart) (outer (build (conj (doall (map inner (@down-cableset termpart))) (term-predicate termpart)) 
                                        :Propositional
                                        {}
                                        #{}))
    ;(arbitraryTerm? termpart) (outer (build-variable (list 'every (:var-label termpart) (map inner @(:restriction-set termpart))))) 
    (atomicTerm? termpart) (outer termpart)
    (set? termpart) (set (doall (map inner termpart)))
    :else (error (str "Term contains unknown parts (" termpart ")"))))

(defn term-prewalk
  [f term & {:keys [ignore-type with-restrictions]}]
  (term-walk 
    (fn [t] (term-prewalk f t :ignore-type ignore-type :with-restrictions with-restrictions)) 
    identity (f term) :ignore-type ignore-type :with-restrictions with-restrictions))


(defn term-prewalk-test
  [term]
  (term-prewalk (fn [x] (print "Walked: ") (prn x) x) term :with-restrictions true))

(defn term-prewalk-test2
  [term]
  (term-prewalk (fn [x] (when (term? x) (print "Walked: ") (prn x)) x) term))

(defn- flatten-term-helper
  "Takes a term, and recursively explores its down-cablesets and optionally
   its restrictions to build a complete set of subterms."
  ([term seen vars?]
    (cond 
      (seen term) '()
      (molecularTerm? term) (flatten (conj (map #(flatten-term-helper % (conj seen term) vars?) (@down-cableset term)) term))
      (and vars? (variableTerm? term)) (flatten (conj (map #(flatten-term-helper % (conj seen term) vars?) (@restriction-set term)) term))
      (atomicTerm? term) (list term)
      (set? term) (flatten (map #(flatten-term-helper % seen vars?) term)))))

;; vars? = true is obviously a little slower, so only do it when needed.
(defn flatten-term
  "Takes a term, and recursively explores its down-cablesets (and optionally
   restrictions) to build a complete set of subterms."
  [term & {:keys [vars?] :or {vars? false}}]
  (disj (set (flatten-term-helper term #{} vars?)) term))

(defn get-antecedents
  [term]
  (let [slot-map (cf/dcsRelationTermsetMap term)]
    (case (type-of term)
      :zinc.core/Conjunction
      (get slot-map (slot/find-slot 'and))
      (:zinc.core/Andor 
       :zinc.core/Disjunction 
       :zinc.core/Xor
       :zinc.core/Nand)
      (get slot-map (slot/find-slot 'andorargs))
      (:zinc.core/Thresh
       :zinc.core/Equivalence)
      (get slot-map (slot/find-slot 'threshargs))
      (:zinc.core/Numericalentailment
       :zinc.core/Implication)
      (get slot-map (slot/find-slot 'ant))
      nil)))

(defn get-vars
  "Returns the vars in the given term, or, if the term is a rule
   returns the intersection of variables in its antecedents. Optionally
   traverses inside variables looking for inner variables."
  [term & {:keys [inner-vars?] :or {inner-vars? false}}]
  (if-let [ants (get-antecedents term)]
    (apply set/intersection (map #(set (filter variable? (flatten-term % :vars? inner-vars?))) ants))
    (set (filter variable? (flatten-term term :vars? inner-vars?)))))
