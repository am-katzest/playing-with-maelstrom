#!/usr/bin/env bb
(ns crdt-messages
  (:require [crdt-gossip :as gossip]
            [crdt :as crdt]))

(defrecord
    CRDT-message-set
    [v]
    crdt/CRDT
    (value [this]
      (:v this))
    (update [this _ _ arg]
      (update this :v conj arg))
    (merge [this other]
      (update this :v into (:v other)))
    (newer? [this other]
      (boolean (seq (remove (:v other) (:v this)))))
    (coerce [this other]
      (cond
        (:v other) (crdt/coerce this (:v other))
        (seqable? other) (->CRDT-message-set (into #{} other))
        :else (assert false "wrong shape"))))
