#!/usr/bin/env bb
(ns crdt-pn-counter
  (:require [crdt :as crdt]
            [crdt-g-counter :as g]
            [crdt-tuple :as tuple]))

;; uses a tuple of g-counters for increments and decrements respectively
(defrecord
    CRDT-pn-counter
    [tuple]
    crdt/CRDT
    (value [this]
      (let [[additions subtractions] (crdt/value (:tuple this))]
        (- additions subtractions)))
    (update [this me value]
      (update this :tuple
              (fn [tuple]
                (crdt/update tuple nil
                             (fn [[additions subtractions]]
                               [(crdt/update additions me (if (pos? value) (abs value) 0))
                                (crdt/update subtractions me (if (neg? value) (abs value) 0))])))))
    (merge [this other]
      (->CRDT-pn-counter (crdt/merge (:tuple this) (:tuple other))))
    (newer? [this other]
      (crdt/newer? (:tuple this) (:tuple other)))
    (coerce [this other]
      (->CRDT-pn-counter (crdt/coerce (:tuple this) (:tuple other)))))

(defn >> [& args] (->CRDT-pn-counter
                (let [[names adds subs] (if (seq args) (apply mapv vector (partition 3 args)) [[] [] []])
                      make-g-counter #(g/map->CRDT-g-counter (into {} (map vector names %)))]
                  (tuple/->CRDT-tuple
                   [(make-g-counter adds)
                    (make-g-counter (map abs subs))]))))

(def zero (>>))
