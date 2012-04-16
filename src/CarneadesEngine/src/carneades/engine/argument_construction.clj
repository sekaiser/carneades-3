;;; Copyright (c) 2011 Fraunhofer Gesellschaft
;;; Licensed under the EUPL V.1.1

(ns ^{:doc "Argument construction using generators."}
  carneades.engine.argument-construction
  (:use clojure.set
        clojure.pprint
        carneades.engine.statement
        carneades.engine.unify
        carneades.engine.argument-graph
        carneades.engine.argument
        carneades.engine.argument-generator
        carneades.engine.argument-builtins))

(defrecord ArgumentTemplate 
  [guard       ; term with all unbound variables of the argument
   instances   ; set of ground terms matching the guard
   argument])  ; partially instantiated argument

(defn make-argument-template
   [& values]
   (let [m (apply hash-map values)]
   (merge (ArgumentTemplate. 
             nil    ; guard
             #{}    ; instances
             nil)   ; argument
          m)))

(defrecord Goal
  [issues         ; (seq-of literals)  ; open issues
   closed-issues  ; set of literals
   substitutions  ; (term -> term) map
   depth])        ; int  

(defn make-goal
   [& values]
   (let [m (apply hash-map values)]
   (merge (Goal. 
             ()     ; issues
            #{}     ; closed issues
             {}     ; substitutions
             0)     ; depth
          m)))

(defrecord ACState        ; argument construction state    
  [goals                  ; (symbol -> goal) map, where the symbols are goal ids
   open-goals             ; set of goal ids (todo: change to a priority queue)
   graph                  ; argument-graph 
   arg-templates          ; (symbol -> argument template) map; symbols are template ids
   asm-templates])        ; vector of non-ground literals

(defn make-acstate
  [& values]
  (let [m (apply hash-map values)]
    (merge (ACState. 
             {}     ; goals
             '()    ; open goals
             (make-argument-graph) 
             {}     ; argument templates
             [])    ; assumption templates
           m)))

(defn initial-acstate
  "literal argument-graph -> ac-state"
  [issue ag]
  (let [goal-id (gensym "g")]
    (make-acstate
      :goals {goal-id (make-goal :issues (list issue))}
      :open-goals (list goal-id)  ; #{goal-id}
      :graph ag)))


(defn- add-goal
  "ac-state goal -> ac-state"
  [state1 goal]
  ; (println "add-goal")
  ; (pprint goal)
  (let [id (gensym "g")]
    (assoc state1
           :goals (assoc (:goals state1) id goal)
           :open-goals (concat (:open-goals state1) (list id))))) ; breadth-first

(defn- remove-goal
  "ac-state symbol -> ac-state"
  [state1 goal-id]
  (assoc state1 
         :open-goals (rest (:open-goals state1))   
         :goals (dissoc (:goals state1) goal-id)))
                                                                                         
(defn- update-issues
  "ac-state goal response -> ac-state
   Add a goal to the state by replacing the first issue of the parent goal
   with the issues of the response. The depth of the parent goal is incremented in this new goal."
  [state1 g1 response]
  ; (println "process premises")
  (let [arg (:argument response)
        subs (:substitutions response)
        closed-issues (conj (:closed-issues g1) 
                            (apply-substitutions subs (first (:issues g1))))]
    (if (nil? arg)
      (add-goal state1 
                (make-goal 
                  :issues (rest (:issues g1))
                  :closed-issues closed-issues
                  :substitutions subs
                  :depth (inc (:depth g1))))
      (let [conclusion (literal->sliteral (conclusion-literal arg))] 
        ;; undercutter `(~'undercut ~(:id arg))]
        (add-goal state1 
                  (make-goal 
                    ; pop the first issue and add issues for the
                    ; premises and exceptions of the argument to the beginning for
                    ; depth-first search
                    :issues (concat (map (fn [p] (literal->sliteral (premise-literal p)))
                                         (concat (:premises (:argument response))
                                                 (:exceptions (:argument response))))
                                    ; (list undercutter)                              
                                    (rest (:issues g1)))
                    :closed-issues closed-issues
                    :substitutions subs
                    :depth (inc (:depth g1))))))))
  

(defn- add-instance
  "map symbol term -> map"
  [arg-template-map key term]
  ; (pprint "add instance")
  (let [arg-template (get arg-template-map key)]
    (assoc arg-template-map key 
           (assoc arg-template 
                  :instances (conj (:instances arg-template) term)))))
  
(defn- apply-arg-templates
  "ac-state goal response -> ac-state
   Apply the argument templates to the substitutions of the response, adding
   arguments to the argument graph of the ac-state for all templates with ground
   guards, if the instance is new.  Add the new instance to the set of instances 
   of the template. Add an undercutter goal for each argument added to the graph."
  [state1 goal response]
  (let [subs (:substitutions response)]
    ; (printf "subs: %s\n" subs)
    (reduce (fn [s k]
              (let [template (get (:arg-templates s) k)
                    ; _ (printf "trying scheme: %s\n" (:scheme (:argument template)))
                    ; _ (printf "guard: %s\n" (:guard template))
                    trm (apply-substitutions subs (:guard template))]
                (if (or (not (ground? trm))
                        (contains? (:instances template) trm))
                  (do ; (printf "%s is a previous instance or not ground.\n" trm)
                      s)
                  (let [arg (instantiate-argument (:argument template) subs)
                        schemes (schemes-applied (:graph state1) (:conclusion arg))]
                    (if (contains? schemes (:scheme arg)) 
                      ; the scheme has already been applied to this issue 
                      (do ; (printf "previous scheme: %s\n" (:scheme arg))
                           s)
                      (do 
                        ; (printf "new instance %s of scheme %s .\n" trm (:scheme arg))
                        (-> s
                          (assoc 
                            :graph (enter-arguments (:graph s) (cons arg (make-undercutters arg)))
                            :arg-templates (add-instance (:arg-templates s) k trm))
                          (add-goal (assoc goal 
                                           :issues (list `(~'undercut ~(:id arg)))
                                           :depth (inc (:depth goal)))))))))))
            state1
            (keys (:arg-templates state1)))))

(defn- apply-asm-templates
  "ac-state goal substitutions -> ac-state"
  [state1 g1 subs]
  ; (println "state1:" state1)
  ; (println "asm-templates: ")
  ; (pprintln (:asm-templates state1))
  (reduce (fn [state2 template] 
            (let [ag (:graph state2)
                  asm (apply-substitutions subs template)]
              ; (println "asm: " asm)
              (if (not (ground? asm))
                state2
                (let [ag2 (assume ag [asm])]
                  (add-goal (assoc state2 :graph ag2)
                            (make-goal 
                              :issues (list (literal-complement (literal->sliteral asm)))
                              :substitutions subs
                              :depth (inc (:depth g1))))))))
          state1
          (:asm-templates state1)))      

(defn- process-assumptions
  "ac-state response -> ac-state
   add the assumptions of the response to the assumption templates"
  [state response]
  (let [subs (:substitutions response)
        asms (:assumptions response)]
    (if (or (nil? asms) (empty? asms))
      state
      (assoc state :asm-templates 
             (concat (:asm-templates state) 
                     (map (fn [asm] (apply-substitutions subs asm))
                          asms))))))

(defn- process-argument
  "ac-state response -> ac-state"
  [state1 response]
  (let [arg (:argument response)]
    ; (printf "process-argument: %s\n" (:scheme arg))
    (if (nil? arg)
      state1
      (assoc state1 
             :arg-templates 
             (assoc (:arg-templates state1) 
                    ; (or (:scheme arg) 
                    (gensym "at")
                    (make-argument-template
                      :guard `(~'guard ~@(argument-variables arg))
                      :instances #{}
                      :argument arg))))))
         
(defn- apply-response
  "ac-state goal response -> ac-state"
  [state1 goal response]
  ; (printf "apply response ===============================\n")
  (-> state1
      (update-issues goal response)
      (process-argument response)
      (process-assumptions response) 
      (apply-arg-templates goal response)
      (apply-asm-templates goal (:substitutions response))))

(defn select-random-member
  "set -> any
   Select and return a random member of a set"
  [set] 
  (let [sq (seq set)] 
    (nth sq (rand-int (count sq)))))

(defn- generate-subs-from-basis
  "argument-graph -> argument-generator"
  [ag1]
  (reify ArgumentGenerator
    (generate [this goal subs]
              (reduce (fn [l wff]
                        (let [subs2 (unify goal (literal-atom wff) subs)]
                          ; (println "atom: " (literal-atom wff))
                          (if (empty? subs2)
                            l
                            (conj l (make-response subs2 () nil)))))
                      []
                      (basis ag1)))))

(defn- reduce-goal
  "ac-state symbol generators -> ac-state
reduce the goal with the given id"
  [state1 id generators1]
  ;; (pprint "reduce-goal")
  ;; Remove the goal from the state. Every goal is reduced at most once. The remaining issues of the goal
  ;; are passed down to the children of the goal, so they are not lost by removing the goal.
  (let [goal (get (:goals state1) id),
        state2 (remove-goal state1 id)]
    ; (printf "issues: %s\n" (vec (:issues goal)))
    ; (printf "closed issues: %s\n" (:closed-issues goal))
    (if (empty? (:issues goal))
      state2 ; no issues left in the goal
      (let [issue (apply-substitutions (:substitutions goal) (first (:issues goal)))]
        (if (contains? (:closed-issues goal) issue)
          ;; the issue has already been handled for this goal and closed
          ;; Add a goal for the remaining issues and return
          (add-goal state2 (assoc goal :issues (rest (:issues goal))))
          ;; close the selected issue in state3 and apply the generators to the issue and its complement
          ;; Rebuttals are constructed even if no pro arguments can be found
          ;; This has the advantage that the same argument graph is constructed for the
          ;; issue P as the issue (not P). The positive or negative form of the issue or query is no longer
          ;; relevant for the purpose of argument construction. But it is still important
          ;; for argument evaluation, where burden of proof continues to play a role.
          (let [generators2 (concat (list (generate-subs-from-basis (:graph state2)))
                                    generators1)]
            ; (println "issue: " issue)
            (let [responses (apply concat (map (fn [g]
                                                 (concat (generate g issue
                                                                   (:substitutions goal))
                                                         (generate g (literal-complement issue)
                                                                   (:substitutions goal))))
                                               generators2))]
             ;  (println "responses: " (count responses))
              (reduce (fn [s r] (apply-response s goal r))
                      state2
                      responses))))))))

(defn- reduce-goals
  "ac-state integer (seq-of generator) -> ac-state
   Construct arguments for both viewpoints and combine the arguments into
   a single argument graph of a ac-state.  All arguments found within the given
   resource limits are included in the argument graph of the resulting ac-state."
  [state1 max-goals generators]
  ; (pprint "reduce-goals")
  (if (or (empty? (:open-goals state1))
          (<= max-goals 0))
    state1
    (let [id (first (:open-goals state1))]   
      (if (not id)
        state1 
        (recur (reduce-goal state1 id generators) 
               (dec max-goals) 
               generators)))))

(defn- notify-observers
  "Informs generators satisfying the ArgumentConstructionObserver protocol that
   the construction is finished."
  [generators]
  (doseq [generator generators]
    (when (satisfies? ArgumentConstructionObserver generator)
      (finish generator))))

(defn construct-arguments
  "argument-graph literal int (coll-of literal) (seq-of generator) -> argument-graph
   Construct an argument graph for both sides of an issue."
  ([ag1 issue max-goals facts generators1]
    (let [ag2 (accept ag1 facts)
          generators2 (concat (list (builtins)) generators1)
          graph (:graph (reduce-goals (initial-acstate issue ag2) 
                                      max-goals 
                                      generators2))]
    (notify-observers generators2)
    graph))
  ([issue max-goals facts generators]
      (construct-arguments (make-argument-graph) issue max-goals facts generators)))

