#!/usr/bin/env bb
(ns echo
  (:require [protocol :as p]))

(defn echo [msg body _]
  (->> {:echo (:echo body)
       :type "echo_ok"}
       (p/reply msg)
       p/send!))

(p/initialize)
(p/run-router {"echo" echo})
