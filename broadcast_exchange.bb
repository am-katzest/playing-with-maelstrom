#!/usr/bin/env bb
(ns broadcast-exchange
  (:require [protocol :as p]))

(def topology (promise))

(def neighbors (future (@p/my-id @topology)))

(let [neighbor-loop (future (atom (cycle @neighbors)))]
  (defn pick-neighbor []
    (first (swap! @neighbor-loop rest))))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))


(def messages (atom #{}))

(defn send-update! []
  (p/send-to! (pick-neighbor) :update :messages @messages))

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
  (p/reply! :read_ok :messages @messages))


(defn broadcast [{:keys [message]}]
  (swap! messages conj message)
  (p/reply! :broadcast_ok))

(defn receive-update [body]
  (let [incoming (into #{} (:messages body))
        old-messages @messages]
    (swap! messages #(into % incoming))
    ;; if we have any messages they don't have...
    (when-not (empty? (remove incoming old-messages))
      ;; send them an update back
      (p/reply! :update :messages @messages)))
  ;; not thread safe, but it's probabilistic anyway
  )

(p/initialize)
(p/run-router {:topology  read-topology
               :read read-handler
               :update receive-update
               :broadcast broadcast})
