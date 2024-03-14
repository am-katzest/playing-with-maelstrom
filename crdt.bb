#!/usr/bin/env bb
(ns crdt
  (:refer-clojure :exclude [update merge]))

;; add exclude thing
(defprotocol CRDT
  (value [this])
  (update [this identity arg])          ;only makes sense when trivial
  (newer? [this other]) ; returns true if `this` is younger in any way
  (merge [this other])
  (coerce [this other]))

(defn all-keys [a b]
  (-> #{}
      (into (keys a))
      (into (keys b))))
