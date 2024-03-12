#!/usr/bin/env bb
;;copied babashka book
(require '[clojure.test :as t]
         '[babashka.classpath :as cp])

(cp/add-classpath ":test")

(require 'test.crdt-messages-test
         'test.crdt-g-counter-test
         'test.json-resistance-test)

(def test-results
  (t/run-tests 'test.crdt-messages-test
               'test.crdt-g-counter-test
               'test.json-resistance-test))

(let [{:keys [fail error]} test-results]
  (when (pos? (+ fail error))
    (System/exit 1)))
