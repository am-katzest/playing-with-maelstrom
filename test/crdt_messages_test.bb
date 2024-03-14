#!/usr/bin/env bb
(ns test.crdt-messages-test
  (:require [crdt-messages :as s]
            [crdt :as crdt]
            [clojure.test :refer [deftest is]]))

(defn >> [& args] (s/->CRDT-message-set (into #{} args)))

(deftest merging-test
  (is (= (>>) (crdt/merge (>>) (>>))))
  (is (= (>> 1) (crdt/merge (>> 1) (>> 1))))
  (is (= (>> 1) (crdt/merge (>> 1) (>>))))
  (is (= (>> 1 2) (crdt/merge (>> 1) (>> 2))))
  (is (= (>> 1 2) (crdt/merge (>> 1) (>> 1 2))))
  (is (= (>> 1 2) (crdt/merge (>>) (>> 1 2)))))

(deftest value-test
  (is (= #{1 2 3} (crdt/value (crdt/merge (>> 1 2) (>> 3))))))

(deftest coercion-test
  (is (= (>> 3) (crdt/coerce (>>) #{3})))
  (is (= (>> 3) (crdt/coerce (>>) [3])))
  (is (thrown? AssertionError (crdt/coerce (>>) 3))))

(deftest update-test
  (is (= (>> :a :b) (crdt/update (>> :a) nil :b)))
  (is (= (>> :a) (crdt/update (>>) nil :a))))

(deftest ordering-test
  (is (not (crdt/newer? (>> :a :b :c) (>> :a :b :c))))
  (is (not (crdt/newer? (>> :a :b) (>> :a :b :c))))
  (is (crdt/newer? (>> :a :b) (>> :a)))
  (is (not (crdt/newer? (>>) (>>))))
  (is (crdt/newer? (>> :a) (>>)))
  (is (crdt/newer? (>> :a) (>> :b)))
  (is (crdt/newer? (>> :b) (>> :a))))
