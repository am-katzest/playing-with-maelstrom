#!/usr/bin/env bb
(ns gossip
  (:require [protocol :as p]
            [clojure.core.async :as a]
            [crdt :as crdt]))


(def neighbor-stream (a/chan 3))
(a/go
  (doseq [x (->> @p/nodes shuffle (remove #{@p/my-id}) cycle)]
    (a/>! neighbor-stream x)))

(def state (atom nil))

(defn send-update! []
  (p/send-to! (a/<!! neighbor-stream) :update :state @state))

(defn start-updater! []
  (future
    (loop []
      (Thread/sleep (+ 500 (rand-int 500)))
      (send-update!)
      (recur))))

(defn read-handler [_]
  (send-update!)
  (p/reply! :read_ok :value (crdt/value @state)))


(defn broadcast [key]
  (fn  [body]
    (swap! state crdt/update @p/my-id (key body))
    (p/reply! :add_ok)))

(defn receive-update [body]
  (let [incoming (crdt/coerce @state (:state body))]
    (swap! state crdt/merge incoming)
    (when (crdt/newer? @state incoming)
      ;; there are data races but none of them matter,
      ;; receiving another update *here* would make it even better
      (p/reply! :update :state @state)))
  )
(defn spread [initial-crdt update-fn]
  (reset! state initial-crdt)
  (p/initialize)
  (start-updater!)
  (p/run-router {:read read-handler
                 :update receive-update
                 :add (broadcast update-fn)}))
