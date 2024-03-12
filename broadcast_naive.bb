#!/usr/bin/env bb
(ns broadcast-naive
  (:require [protocol :as p]))
(def topology (promise))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))

(defn read-topology [_ body _]
  (deliver topology (parse-topology (:topology body)))
  (p/reply! :topology_ok))

(def messages (atom #{}))

(defn read-handler [_ _ _]
  (p/reply! :read_ok :messages @messages))

(defn broadcast [_ {:keys [message]} _]
  (binding [*out* *err*] (println message))
  (when-not (@messages message)
    (doseq [neighbor (@p/my-id @topology)]
      (p/send-to! neighbor :msg-exchange :message message)))
  (swap! messages conj message)
  (p/reply! :broadcast_ok))

(p/initialize)
(p/run-router {:topology read-topology
               :read read-handler
               :broadcast broadcast
               :error (constantly nil)
               :broadcast_ok (constantly nil)
               :msg-exchange broadcast})
