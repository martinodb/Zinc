(ns zinc.test.mapper-benchmark
  (:require [zinc.core.contexts :as ct]
            [zinc.core.caseframes :as cf]
            [zinc.core.printer :as print]
            [zinc.core.relations :as slot]
            [zinc.core :as zinc]
            [zinc.core.build :as build]
            [zinc.snip :as snip]
            [clojure.set :as set]
            [clojure.string :as str]
            [zinc.core.snuser :as snuser]))

(declare sneps3kbtozinc semtypesToObjLang)

(def runtime (atom 0))

(defn log-elapsed
  [start-time]
  (swap! runtime + (/ (- (. java.lang.System (clojure.core/nanoTime)) start-time) 1000000.0)))

(defn reset-benchmark
  []
  (def runtime (atom 0)))

(defn print-time
  []
  (println "Run Time: " @runtime))

(def adopt-order
  '[properNounToName1
    properNounToName2
    organizationHasName
    nnName
    nounPhraseToInstance
    eventToInstance
    pluralNounToGroup
    subjAction
    dobjAction
    prepToRelation
    nnToModifier
    amodToModifier])

(defn loadkb
  [msgfile framefile rulefile]
  (load-file framefile)
  (sneps3kbtozinc msgfile)
  (semtypesToObjLang)
  (load-file rulefile))

(defn synsem-one-file
  [msgfile framefile rulefile]
  (snuser/clearkb true)
  (loadkb msgfile framefile rulefile)
  (let [start-time (. java.lang.System (clojure.core/nanoTime))]
    (snuser/adopt-rules adopt-order)
    (log-elapsed start-time)
    (print-time)))

(defn synsem-benchmark
  [msgfolder framefile rulefile]
  (reset-benchmark)
  (doseq [f (file-seq (clojure.java.io/file msgfolder))]
    (when-not (.isDirectory f)
      (synsem-one-file (.getPath f) framefile rulefile))))


;;; Util fns

(defn semtypesToObjLang
  []
;  (doseq [[c ps] (:parents @zinc/semantic-type-hierarchy)
;          p ps]
;    (snuser/assert `(~'Isa (~'every ~'x (~'Isa ~'x ~(name c))) ~(name p))))
  (let [terms (filter zinc/atomicTerm? (vals @zinc/TERMS))]
    (doseq [t terms]
      (snuser/assert ['Isa t (name (zinc/semantic-type-of t))]))))

(defn typeToGeneric
  [typestr]
  (let [typeseq (read-string typestr)]
    (str (list 'Isa (list 'every 'x (list 'Isa 'x (second typeseq))) (nth typeseq 2)))))

(defn sneps3kbtozinc
  [filename]
  (let [filestr (-> (slurp filename)
                  (str/replace "ct:assert" "zinc.core.snuser/assert")
                  (str/replace " 'DefaultCT :origintag :hyp" "")
                  (str/replace "|" "\"")
                  (str/replace "\"\"\"" "\"\\\"\"")
                  (str/replace "(load" "(comment")
                  (str/replace #"\(zinc.core.snuser/assert '\(Message.*?\)\)" "") ;; Not using the message assertion, lets ignore it since it has weird parsing requirements.
                  (str/replace #"\(zinc.core.snuser/assert '\(SyntacticCategoryOf POS.*?\)\)" "")
                  (str/replace "(in-package :snuser)" "(in-ns 'csneos.core.snuser)")
                  (str/replace "Action" "Action1"))
        typestrings (re-seq #"\(Type\s\S+\s\S+?\)" filestr)
        filestr (loop [typestrings typestrings
                      fs filestr]
                 (if (seq typestrings)
                  (recur (rest typestrings)
                        (str/replace fs (first typestrings) (typeToGeneric (first typestrings))))
                  fs))
        mgrsstrings (set (re-seq #"\d+[A-Z]+\d+" filestr))
        filestr (loop [mgrsstrings mgrsstrings
                       fs filestr]
                  (if (seq mgrsstrings)
                    (recur (rest mgrsstrings)
                           (str/replace fs (first mgrsstrings) (str \" (first mgrsstrings) \")))
                    fs))]
    (println filestr)
    (load-string filestr)))
    

  
  