(ns test.json-resistance-test
  (:require  [crdt :as crdt]
             [crdt-g-counter :as g]
             [crdt-messages :as m]
             [crdt-pn-counter :as pn]
             [crdt-tuple :as tuple]
             [cheshire.core :as json]
             [clojure.test :refer [is deftest]]))

(defmacro remains-the-same [original]
  `(is (= ~original
          (crdt/coerce ~original
                       (json/parse-string
                        (json/generate-string ~original) true)))))
(json/generate-string (m/->CRDT-message-set {}))
(deftest crdt-messages-test
  (remains-the-same (m/->CRDT-message-set #{}))
  (remains-the-same (m/->CRDT-message-set #{1 5})))

(deftest crdt-g-counter-test
  (remains-the-same (g/map->CRDT-g-counter {}))
  (remains-the-same (g/map->CRDT-g-counter {:a 3})))

(deftest crdt-tuple-test
  (remains-the-same (tuple/->CRDT-tuple []))
  (remains-the-same (tuple/->CRDT-tuple [(g/map->CRDT-g-counter {})]))
  (remains-the-same (tuple/->CRDT-tuple [(g/map->CRDT-g-counter {}) (g/map->CRDT-g-counter {:a 3}) (m/->CRDT-message-set #{1 5})])))

(deftest crdt-pn-counter-test
  (remains-the-same pn/zero)
  (remains-the-same (pn/->CRDT-pn-counter
                     (tuple/->CRDT-tuple
                      [(g/map->CRDT-g-counter {:a 3}) (g/map->CRDT-g-counter {:a 3})]))))
