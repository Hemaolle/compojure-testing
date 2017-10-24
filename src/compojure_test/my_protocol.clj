(ns compojure-test.my-protocol)

(defprotocol my-protocol
  (execute [x]))

(extend-protocol my-protocol
  clojure.lang.IPersistentMap
  (execute [map]
    (assoc map :value-2 2)))