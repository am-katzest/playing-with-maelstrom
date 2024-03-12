#!/usr/bin/env bb
(ns echo
  (:require [protocol :as p]))

(defn echo [body]
  (p/reply! :echo_ok :echo (:echo body)))

(p/initialize)
(p/run-router {:echo echo})
