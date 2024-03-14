#!/usr/bin/env bb
(ns broadcast
  (:require [gossip :as g]
            [crdt-messages :as m]
            [crdt :as crdt]))

(g/spread m/zero
          (fn [messages {:keys [message]}]
            (crdt/update messages nil message)))
