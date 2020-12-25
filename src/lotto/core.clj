(ns lotto.core
  (:gen-class)
  (:require [clojure.data.json :as json]))

(->> (repeatedly #(rand-int 45))
     (take 10)
     (filter #(> % 0))
     (into #{})
     (take 6)
     (sort))

(defn get-lotto-json [no]
  (json/read-str
   (slurp
    (str "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" no))))

(defn get-lotto [no]
  (json/read-str
   (slurp
    (str "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" no))
   :key-fn keyword))

(defn save-lotto []
  (->> (for [i (range 1 943)] (get-lotto-json i))
       (into [])
       (spit "lotto.json")))

(def all-lotto (json/read-str (slurp "lotto.json") :key-fn keyword))

;; json의 drwtNo를 가져오기 위한 구조를 반환
(defn get-lotto-no-keywords []
  (map #(keyword (str "drwtNo" %)) (range 1 7)))

;; json 으로 되어 있는 값들을 [1 2 3 4 5 6]의 형태로 바꾸기
(let [no (get-lotto-no-keywords)]
  (reduce #(conj %1 (vals (select-keys %2 no))) [] all-lotto))

;; hash-map에서 value가 제일 큰 값 가져오기
(defn- get-big-number [coll]
  (reduce #(if (> (val (first %1)) (val (first %2))) %1 %2) (first coll) (rest coll)))

;; 큰 값을 가져오기
(defn get-big-lotto [digit lotto-list]
  (->> (map #((keyword (apply str ["drwtNo" digit])) %) lotto-list)
       (sort)
       (partition-by identity)
       (map #(hash-map (first %) (count %)))
       (get-big-number)))

;; 이 함수를 이용해서 어떻게 [1 2 3 4 5 6]의 형태를 
;; 만들 수 있는지 생각해보기
(->> (for [i (range 1 7)]
       (get-big-lotto i all-lotto))
     (map #(key (first %))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (json/read-str
   (slurp "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=940")))

