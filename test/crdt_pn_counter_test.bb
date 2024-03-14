#!/usr/bin/env bb
(ns test.crdt-pn-counter-test
  (:require [crdt :as crdt]
            [crdt-pn-counter :as pn]
            [crdt-tuple :as tuple]
            [crdt-g-counter :as g]
            [clojure.test :refer [deftest is]]))

(defn >> [& args] (pn/->CRDT-pn-counter
                (let [[names adds subs] (if (seq args) (apply mapv vector (partition 3 args)) [[] [] []])
                      make-g-counter #(g/map->CRDT-g-counter (into {} (map vector names %)))]
                  (tuple/->CRDT-tuple
                   [(make-g-counter adds)
                    (make-g-counter (map abs subs))]))))
(deftest merging-test
  (is (= (>>) (crdt/merge (>>) (>>))))
  (is (= (>> :a 5 -8) (crdt/merge (>> :a 5 -8) (>> :a 5 -8))))
  (is (= (>> :a 5 -8) (crdt/merge (>> :a 5 -8) (>> :a 5 0))))
  (is (= (>> :a 5 -3) (crdt/merge (>> :a 5 -3) (>>))))
  (is (= (>> :a 5 -5) (crdt/merge (>> :a 3 -5) (>> :a 5 -3))))
  (is (= (>> :a 5 -5 :b 3 -3) (crdt/merge (>> :a 3 -5) (>> :a 5 -3 :b 3 -3)))))

(deftest value-test
  (is (= -8 (crdt/value (crdt/merge (>> :a 2 -5) (>> :b 3 -8))))))

(deftest update-test
  (is (= (>> :a 3 -3) (crdt/update (>> :a 3 3) :a 0)))
  (is (= (>> :a 6 -3) (crdt/update (>> :a 3 3) :a 3)))
  (is (= (>> :a 3 -6) (crdt/update (>> :a 3 3) :a -3)))
  (is (= (>> :a 3 -3 :b 0 -3) (crdt/update (>> :a 3 -3) :b -3))))

(deftest ordering-test
  (is (not (crdt/newer? (>> :a 3 6) (>> :a 3 7))))
  (is (not (crdt/newer? (>> :a 3 6) (>> :a 3 7)))))
