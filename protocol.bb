#!/usr/bin/env bb
(ns protocol
  (:require [cheshire.core :as json]
            [clojure.core.async :as a]))


(def my-id (promise))
(def nodes (promise))

(defn start-reader []
  (let [chan (a/chan)]
    (a/go
      (doseq [msg (json/parsed-seq *in* true)]
        (a/>! chan [msg (:body msg) (-> msg :body :type)]))
      (a/close! chan))
    chan))

(defn enumerate
  [msg id]
  (assoc-in msg [:body :msg_id] id))

(defn start-writer []
  (let [chan (a/chan)]
    (a/go-loop [id 0]
      (when-let [msg (a/<! chan)]
        (->
         msg
         (enumerate id)
         (assoc :src (name @my-id))
         json/generate-string
         println)
        (flush)
        (recur (inc id))))
    chan))

(def reader (start-reader))
(def writer (start-writer))

(defn send-to [dest body]
  {:dest dest
   :body body})

(defn reply [request body]
  (let [dest (:src request)
        request-id (-> request :body :msg_id)
        body' (assoc body :in_reply_to request-id)]
    (send-to dest body')))

(defn error [request code text]
  (reply request {:type "error"
                  :code code
                  :text text}))

(defn send! [msg] (a/>!! writer msg))

(defn initialize []
  (let [[initial-msg body type] (a/<!! reader)
        {:keys [node_id node_ids]} body]
    (assert (= "init" type))
    (deliver my-id (keyword node_id))
    (deliver nodes (mapv keyword node_ids))
    (send! (reply initial-msg {:type "init_ok"}))))

(def ^:dynamic *request* "no request in progress")

(defn run-router [responders]
  (loop []
    (when-let [[msg body type] (a/<!! reader)]
      (binding [*request* msg]
        (if-let [responder (responders type)]
          (responder msg body type)
          (send! (error msg 10 (format "no function bound to type: %s" type)))))
      (recur))))

(defn reply! [type & kvs]
  (let [payload {:type (name type)}]
    (->> (if kvs (apply assoc payload kvs) payload)
         (reply *request*)
         (send!))))
