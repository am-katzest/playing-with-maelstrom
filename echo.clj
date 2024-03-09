#!/usr/bin/env bb
(ns echo
  (:require [protocol :as p]))

(defn echo [msg body _]
  (p/send! (p/reply msg {:echo (:echo body)})))

(p/initialize)
(p/run-router {"echo" echo})
