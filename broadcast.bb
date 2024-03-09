#!/usr/bin/env bb
(ns broadcast
  (:require [protocol :as p]))
(def topology (promise))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))

(defn read-topology [msg body _]
  (deliver topology (parse-topology (:topology body)))
  (->> {:type "topology_ok"}
       (p/reply msg)
       p/send!))

(p/initialize)
(p/run-router {"topology" read-topology})
