#!/usr/bin/env bb
(ns test.crdt-g-counter-test
  (:require [crdt :as crdt]
            [cheshire.core :as json]
            [crdt-g-counter :as g :refer [>>]]
            [clojure.test :refer [deftest is]]))

(deftest merging-test
  (is (= (>>) (crdt/merge (>>) (>>))))
  (is (= (>> :a 5) (crdt/merge (>> :a 5) (>> :a 5))))
  (is (= (>> :a 5) (crdt/merge (>> :a 5) (>>))))
  (is (= (>> :a 5 :b 7) (crdt/merge (>> :a 5) (>> :b 7))))
  (is (= (>> :a 5 :b 7) (crdt/merge (>> :a 5) (>> :b 7 :a 3))))
  (is (= (>> :a 5 :b 7) (crdt/merge (>> :a 5 :b 7) (>>)))))

(deftest value-test
  (is (= 3 (crdt/value (>> :b 3))))
  (is (= 7 (crdt/value (>> :a 3 :b 4))))
  (is (= 0 (crdt/value (>>)))))

(deftest coercion-test
  (is (= (>> :a 3) (crdt/coerce (>>) {:a 3})))
  (is (= (>>) (crdt/coerce (>>) {})))
  (is (thrown? AssertionError (crdt/coerce (>>) 3)))
  (is (thrown? AssertionError (crdt/coerce (>>) {"a" 3})))
  (is (thrown? AssertionError (crdt/coerce (>>) {:a :3}))))

(deftest update-test
  (is (= (>> :a 3) (crdt/update (>> :a 3) :a 0)))
  (is (= (>> :a 8) (crdt/update (>> :a 3) :a 5)))
  (is (= (>> :a 8) (crdt/update (>>) :a 8)))
  (is (= (>> :a 3 :b 8) (crdt/update (>> :a 3) :b 8)))
  (is (= (>> :a 3 :b 8) (crdt/update (>> :a 3 :b 5) :b 3))))

(deftest ordering-test
  (is (not (crdt/newer? (>> :a 3 :b 4) (>> :a 3 :b 4))))
  (is (not (crdt/newer? (>>) (>>))))
  (is (not (crdt/newer? (>> :a 2) (>> :a 3))))
  (is (crdt/newer? (>> :a 3) (>> :a 2)))
  (is (not (crdt/newer? (>>) (>> :b 3))))
  (is (crdt/newer? (>> :b 3) (>>)))
  (is (crdt/newer? (>> :a 1) (>> :b 1)))
  (is (crdt/newer? (>> :b 1) (>> :a 1))))
