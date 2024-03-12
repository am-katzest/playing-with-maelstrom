#!/usr/bin/env bb
(ns echo
  (:require [protocol :as p]))

(defn echo [_ body _]
  (p/reply! :echo_ok :echo (:echo body)))

(p/initialize)
(p/run-router {:echo echo})
