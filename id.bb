#!/usr/bin/env bb
(ns id
  (:require [protocol :as p]
            [clojure.core.async :as a]))
;; server allocates ids in blocks
(def server-id (future (first (sort @p/nodes))))
;;is only ever used on the server
(def server-count (atom 0))
(defn give-block [{:keys [amount]}]
  (assert (= @p/my-id @server-id))
  (let [end (swap! server-count + amount)
        start (- end amount)]
    (p/reply! :block_ok :block [start amount])))

;; has our allocated blocks (except for currently unused one
(def block-queue (a/chan 100))

(def id-queue (a/chan))

(def request-size 10)

(def hold-at-least 15)

(defn request-block [amount]
  (p/send-to! @server-id :request_block :amount amount))

(defn should-ask-for-more? [{:keys [remaining outgoing-requests]}]
  (<= (+ outgoing-requests remaining) hold-at-least))

(defn send-requests [state]
  (loop [state state]
    (if-not (should-ask-for-more? state) state
            (do (request-block request-size)
                (recur (update state :outgoing-requests + request-size))))))

(defn receive-block [{:keys [block]}]
  (a/>!! block-queue block))

(defn add-block [state [start amount]]
  (-> state
      (assoc :remaining  amount :id start)
      (update :outgoing-requests - amount)))

(defn consume-id [state]
  (-> state
      (update :id inc)
      (update :remaining dec)))

(a/go-loop [{:keys [id remaining] :as state}
            {:remaining 0
             :id "shouldn't happen"
             :outgoing-requests 0}]
  (if (pos? remaining)
    (do
      (a/>! id-queue id)
      (recur (consume-id state)))
    ;; block ended
    (if (should-ask-for-more? state)
      (recur (send-requests state))
      (recur (add-block state (a/<! block-queue))))))

(defn give-id [_]
  (p/reply! :generate_ok :id (a/<!! id-queue)))

(do
  (p/initialize)
  (p/run-async-router
   {:request_block give-block
    :block_ok receive-block
    :generate give-id}))
