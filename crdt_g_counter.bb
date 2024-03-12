#!/usr/bin/env bb
(ns crdt-g-counter
  (:require [crdt :as crdt]))

(defrecord
    CRDT-g-counter
    []
  crdt/CRDT
    (value [this]
      (let [v (vals this)]
        (if (seq v)
          (apply max v)
          0)))
    (update [this me _ arg]
      (update this me + arg))
    (merge [this other]
      (merge-with max this other))
    (newer? [this other]
      (some (fn [k]
              (let [ours (k this)
                    theirs (k other)]
                (cond
                  (nil? ours) false
                  (nil? theirs) true
                  :else (> ours theirs))))
            (crdt/all-keys this other)))
    (coerce [_ other]
      (assert (map? other))
      (assert (every? keyword?  (keys other)))
      (assert (every? number?  (vals other)))
      (map->CRDT-g-counter other)))
