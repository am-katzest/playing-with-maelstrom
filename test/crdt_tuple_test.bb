#!/usr/bin/env bb
(ns test.crdt-tuple-test
  (:require [crdt-messages :as s :refer [>>]]
            [crdt-tuple :as t]
            [crdt :as crdt]
            [clojure.test :refer [deftest is]]))

(def >>t t/>>)

(deftest merging-test
  (is (= (>>t (>> 1 3) (>> 1 2))
         (crdt/merge (>>t (>> 1 3) (>> 1 2))
                     (>>t (>> 1 3) (>> 1 2)))))
  (is (thrown? AssertionError
               (crdt/merge (>>t (>> 1 3) (>> 1 2))
                           (>>t (>> 1 3) (>> 1 2) (>> 1 2))))))

(deftest value-test
  (is (= [#{1 3} #{1 2}] (crdt/value (>>t (>> 1 3) (>> 1 2))))))

(deftest coercion-test
  (is (= (>>t (>> 3)) (crdt/coerce (>>t (>>)) {:list [[3]]})))
  (is (thrown? AssertionError (crdt/coerce (>>t) {:list [3]}))))

(deftest update-test
  (is (= (>>t (>> 3))
         (crdt/update (>>t (>>)) nil
                      (fn [[elem]]
                        [(crdt/update elem nil 3)]))))
  (is (= (>>t (>> 3) (>> 3 4))
         (crdt/update (>>t (>>) (>> 4)) nil
                      (fn [[elem other]]
                        [(crdt/update elem nil 3)
                         (crdt/update other nil 3)])))))
(deftest ordering-test
  (is (crdt/newer? (>>t (>> :a :b :c) (>> :a :b :c :d))
                   (>>t (>> :a :b :c) (>> :a :b :c))))
  (is (not (crdt/newer? (>>t (>> :a :b :c) (>> :a :b :c))
                        (>>t (>> :a :b :c) (>> :a :b :c :d))))))
