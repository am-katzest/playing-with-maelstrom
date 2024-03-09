#!/usr/bin/env bb
(ns broadcast
  (:require [protocol :as p]))
(def topology (promise))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))

(defn read-topology [_ body _]
  (deliver topology (parse-topology (:topology body)))
  (p/reply! :topology_ok))
(def messages (atom []))

(p/initialize)
(p/run-router {"topology" read-topology})
