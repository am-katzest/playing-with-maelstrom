#!/usr/bin/env bb
(ns crdt-tuple
  (:require [crdt :as crdt]))
;; for implementing other counters
(defrecord
    CRDT-tuple
    [list]
    crdt/CRDT
    (value [this]
      (map crdt/value (:list this)))
    (update [this _  function]
      (->CRDT-tuple (function (:list this))))
    (merge [this other]
      (assert (= (-> this :list count) (-> other :list count)))
      (->CRDT-tuple
       (map crdt/merge (:list this) (:list other))))
    (newer? [this other]
      (reduce #(or %1 %2) (map crdt/newer? (:list this) (:list other))))
    (coerce [this other]
      (cond
        (:list other) (do
                        (assert (= (count (:list this)) (count (:list other))))
                        (crdt/coerce this (:list other)))
        :else (->CRDT-tuple (map crdt/coerce (:list this) other)))))
