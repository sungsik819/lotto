(ns lotto.core
  (:gen-class)
  (:require [lotto.lotto-core :as lotto]))

(->> (repeatedly #(rand-int 45))
     (take 10)
     (filter #(> % 0))
     (into #{})
     (take 6)
     (sort))

(lotto/get-big-values)

(lotto/get-small-values)

(lotto/get-big-lotto-digit)

(defn -main
  "I don't do a whole lot ... yet."
  [& args])

