#!/usr/bin/env bb
(ns broadcast-preplanned
  (:require [protocol :as p]))
(def topology (promise))

(defn parse-topology [t]
  (into {} (map (fn [[k v]] [k (mapv keyword v)]) t)))

(defn read-topology [body]
  (deliver topology (parse-topology (:topology body)))
  (p/reply! :topology_ok))

(def messages (atom #{}))

(defn read-handler [_]
  (p/reply! :read_ok :messages @messages))


(defn shortest-path [from to topology]
  (loop [[current & rest] [[from]]]
    (let [tail (last current)]
      (cond (= to tail) current
            (some? current) (recur (concat rest (map #(conj current %) (topology tail))))
            (nil? current) nil ))))
(defn enmappify "[a b c] -> {a {b {c {}}}}"  [list]
  (reduce (fn [acc x] {x acc}) {(last list) {}} (rest (reverse list))))
;; (enmappify [:a :b :c :d :e])
(defn merge-rec [& args]
  (apply merge-with merge-rec args))
(defn plan-exchange [from targets topology]
  (->> targets
       (remove #{from})
       (map #(shortest-path from % topology))
       (map rest)
       (map enmappify)
       (reduce merge-rec)))

(defn broadcast [{:keys [message]}]
  (swap! messages conj message)
  (p/reply! :broadcast_ok)
  (doseq [[target further] (plan-exchange @p/my-id @p/nodes @topology)]
    (p/send-to! target :msg-exchange
                :message message
                :targets further)))

(defn exchange [{:keys [targets message]}]
  (doseq [[target further] targets]
    (p/send-to! target :msg-exchange
                :message message
                :targets further))
  (swap! messages conj message))

(p/initialize)
(p/run-async-router {:topology  read-topology
               :read read-handler
               :broadcast broadcast
               :msg-exchange  exchange})
