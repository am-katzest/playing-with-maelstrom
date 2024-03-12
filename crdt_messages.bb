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
    (coerce [_ other]
      (assert (seqable? other))
      (->CRDT-message-set (into #{} other))))