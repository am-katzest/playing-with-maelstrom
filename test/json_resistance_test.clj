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
  (remains-the-same (m/>>))
  (remains-the-same (m/>> 1 5)))

(deftest crdt-g-counter-test
  (remains-the-same (g/>>))
  (remains-the-same (g/>> :a 3)))

(deftest crdt-tuple-test
  (remains-the-same (tuple/>>))
  (remains-the-same (tuple/>> (g/>>)))
  (remains-the-same (tuple/>> (g/>>) (g/>> :a 3) (g/>> :a 3))))

(deftest crdt-pn-counter-test
  (remains-the-same pn/zero)
  (remains-the-same (pn/>> :a 3 -3)))
