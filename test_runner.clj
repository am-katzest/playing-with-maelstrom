#!/usr/bin/env bb
;;copied babashka book
(require '[clojure.test :as t]
         '[babashka.classpath :as cp])

(cp/add-classpath ":test")
(def namespaces
  '[test.crdt-messages-test
    test.crdt-g-counter-test
    test.crdt-tuple-test
    test.crdt-pn-counter-test
    test.json-resistance-test])

(apply require namespaces)

(def test-results
  (apply t/run-tests namespaces))

(let [{:keys [fail error]} test-results]
  (when (pos? (+ fail error))
    (System/exit 1)))
