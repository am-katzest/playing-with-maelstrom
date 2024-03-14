#!/usr/bin/env bb
(ns spread-crdt
  (:require [gossip :as g]
            [crdt-messages :as m]
            [crdt-g-counter :as gc]
            [crdt-pn-counter :as pn]))


(def dispatch
  ({:g-set [m/zero :element]
    :g-counter [gc/zero :delta]
    :pn-counter [pn/zero :delta]
    } (keyword (first *command-line-args*))))


(apply g/spread dispatch)
