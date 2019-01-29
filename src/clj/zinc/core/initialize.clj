(in-ns 'zinc.core.snuser)

(defn clearkb
  [& clear?]
  (dosync 
    (let [clearall (first clear?)]
      ;; Stop any ongoing inference. 
      (snip/resetExecutor)
      
      ;; Initialize Contexts
      (ref-set ct/CONTEXTS (hash-map))
      
      ;;Setup default contexts
      (ct/defineContext 'BaseCT
        :docstring "The root of all the contexts."
        :parents nil)
      (ct/defineContext 'OntologyCT
        :docstring "Context containing the semantic type ontology and other ontological
                    assertions which are not subject to belief revision.")
      (ct/defineContext 'DefaultCT
        :docstring "The default current context."
        :parents '(OntologyCT))
      ;; Set current context after the dosync, since 
      ;; it's side-effect producing. 

      ;;Remove term-type maps from semantic types.
      (ref-set zinc/type-map (hash-map))
      (ref-set zinc/support-set (hash-map))
      (ref-set zinc/supported-nodes-set (hash-map))
      (ref-set zinc/primaction (hash-map))


      ;; Initialize the set of terms.
      (ref-set zinc/WFTCOUNT 0)
      (ref-set zinc/TERMS (hash-map))

      ;; Initialize the set of arbitraries.
      (ref-set zinc/ARBITRARIES #{})
      (ref-set zinc/ARBCOUNT 0)

      ;; Initialize the set of indefinites.
      (ref-set zinc/INDEFINITES #{})
      (ref-set zinc/INDCOUNT 0)
      
      ;; Initialize the set of question mark variables.
      (ref-set zinc/QVARS #{})
      (ref-set zinc/QVARCOUNT 0)
      
      ;; Reset term parts:
      (ref-set zinc/i-channels {})
			(ref-set zinc/u-channels {})
			(ref-set zinc/g-channels {})
			(ref-set zinc/ant-in-channels {})
			(ref-set zinc/future-fw-infer {})
			(ref-set zinc/instances {})
			(ref-set zinc/expected-instances {})
			(ref-set zinc/up-cablesetw {})
			(ref-set zinc/support {})
			(ref-set zinc/msgs {})
			(ref-set zinc/restriction-set {})
			(ref-set zinc/dependencies {})
			(ref-set zinc/lattice-node {})
			(ref-set zinc/down-cableset {})
			(ref-set zinc/caseframe {})
   
      ;; Clear cache used by IG.
      (memo-clear! snip/hyp-subst-of-ct?)

      ;; Reinitialize unification tree.
      (build/reset-tree)
      
      ;; Clear the subsumption lattice
      (build/reset-lattice)      

      ;; Remove terms from frames.
      (doseq [cf (seq @cf/CASEFRAMES)]
        (ref-set (:terms cf) (hash-set)))

      ;; Reset slots/frames
      (when clearall         
        ;;Initialize the Semantic Type hierarchy
        (build/initialize-default-hierarchy)
        
        ;; Reinitialize slots
        (ref-set slot/SLOTS (hash-map))
        
        ;; Slots for built-in Propositions
        ;; ===================================
        (defineSlot class :type Category
          :docstring "Points to a Category that some Entity is a member of."
          :negadjust reduce)
        (defineSlot member :type Entity
          :docstring "Points to the Entity that is a member of some Category."
          :negadjust reduce)
        (defineSlot equiv :type Entity
          :docstring "All fillers are coreferential."
          :min 2 :negadjust reduce
          :path (compose ! equiv (kstar (compose equiv- ! equiv))))
        ;(defineSlot closedvar :type Entity
        ;  :docstring "Points to a variable in a closure.")
        (defineSlot proposition :type Propositional
          :docstring "Points to a proposition.")
        
        ;; Slots for Rules
        ;; ===================
        (defineSlot and :type Propositional
          :docstring "Fillers are arguments of a conjunction."
          :min 2 :posadjust reduce :negadjust expand)
        (defineSlot nor :type Propositional
          :docstring "Fillers are arguments of a nor."
          :min 1 :posadjust reduce :negadjust expand)
        (defineSlot andorargs :type Propositional
          :docstring "Fillers are arguments of an andor."
          :min 2 :posadjust none :negadjust none)
        (defineSlot threshargs :type Propositional
          :docstring "Fillers are arguments of a thresh."
          :min 1 :posadjust none :negadjust none)
        (defineSlot thnor :type Propositional
          :docstring "Fillers are arguments of a thnor."
          :min 1 :posadjust reduce :negadjust reduce)
        (defineSlot ant :type Propositional
          :docstring "antecedent for a set."
          :min 1 :posadjust expand :negadjust reduce)
        (defineSlot cq :type Propositional
          :docstring "consequent for a set."
          :min 1 :posadjust reduce :negadjust expand)
        
        ;; Slots for SNeRE
        ;; ===================
        (defineSlot action :type Action
          :docstring "The actions of an act."
          :min 1 :max 1
          :posadjust none :negadjust none)
        
        ;; Slots for condition-action rules
        ;; ================================
        (defineSlot condition :type Propositional 
          :docstring "conditions for a rule."
          :min 1 :posadjust expand :negadjust reduce)
        (defineSlot rulename :type Thing
          :docstring "The name of a rule."
          :min 1 :max 1 :posadjust none :negadjust none)
        (defineSlot subrule :type Policy
          :docstring "subrules for a rule."
          :min 0 :posadjust expand :negadjust reduce)
        
        ;; Reinitialize caseframes
        (ref-set cf/CASEFRAMES (hash-set))
        (ref-set cf/FN2CF (hash-map))
        (ref-set cf/NoviceCaseframes (hash-map))
        (defineCaseframe 'Propositional  '('Isa member class)
          :docstring "[member] is a [class]")
        (defineCaseframe 'Propositional '('Equiv equiv)
          :docstring "[equiv] are all co-referential")
        (defineCaseframe 'Propositional '('and and)
          :docstring "it is the case that [and]")
        (defineCaseframe 'Propositional '('nor nor)
          :docstring "it is not the case that [nor]")
        (defineCaseframe 'Propositional '('thnor thnor)
          :docstring "I don't know that it is the case that [thnor]")
        (defineCaseframe 'Propositional '('andor andorargs))
        (defineCaseframe 'Propositional '('thresh threshargs))
        (defineCaseframe 'Propositional '('if ant cq)
          :docstring "if [ant] then [cq]")
        (defineCaseframe 'Propositional '('close proposition)
          :docstring "[proposition] is closed over [closedvar]")
        (defineCaseframe 'Policy '('rule rulename condition action subrule)
          :docstring "for the rule [name] to fire, [condition] must be matched,
                      then [action] may occur, and [subrule] may be matched."))
      ))
  
  (build/initial-semtypes-to-obj-lang)
  
  ;; Do this outside the dosync (see above).
  (ct/setCurrentContext 'DefaultCT)

  ;; Output message.
  (if (first clear?)
    (println "Knowledge Base cleared. Contexts, slots, caseframes, and semantic types reinitialized.")
    (println "Knowledge Base cleared. Contexts reinitialized.")))