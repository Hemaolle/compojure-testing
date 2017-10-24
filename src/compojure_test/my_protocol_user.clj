(ns compojure-test.my-protocol-user
  (:require [compojure-test.my-protocol]))

(defn use-protocol
  [map]
  (compojure-test.my-protocol/execute map))