#!/usr/bin/env bb
(ns gossip
  (:require [protocol :as p]
            [crdt :as crdt]))

(def topology (promise))

(def neighbors (future (@p/my-id @topology)))

(let [neighbor-loop (future (atom (cycle @neighbors)))]
  (defn pick-neighbor []
    (first (swap! @neighbor-loop rest))))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))

(def state (atom nil))

(defn send-update! []
  (p/send-to! (pick-neighbor) :update :state @state))

(defn start-updater! []
  (future
    (loop []
      (Thread/sleep (+ 1000 (rand-int 1000)))
      (send-update!)
      (recur))))

(defn read-topology [body]
  (->> body :topology parse-topology (deliver topology))
  (p/reply! :topology_ok)
  (start-updater!))

(defn read-handler [_]
  (send-update!)
  (p/reply! :read_ok :messages (crdt/value @state)))


(defn broadcast [update-fn]
  (fn  [body]
    (swap! state update-fn body)
    (p/reply! :broadcast_ok)))

(defn receive-update [body]
  (let [incoming (crdt/coerce @state (:state body))]
    (p/log "%s -- %s -- %s  -- %s" (crdt/value @state) (crdt/value incoming) (crdt/value (crdt/merge @state incoming)) body)
    (swap! state crdt/merge incoming)
    (when (crdt/newer? @state incoming)
      ;; there are data races but none of them matter,
      ;; receiving another update *here* would make it even better
      (p/reply! :update :state @state)))
  )

(defn spread [initial-crdt update-fn]
  (reset! state initial-crdt)
  (p/initialize)
  (p/run-router {:topology  read-topology
                 :read read-handler
                 :update receive-update
                 :broadcast (broadcast update-fn)}))
