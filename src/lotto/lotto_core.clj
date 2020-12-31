(ns lotto.lotto-core
  (:gen-class)
  (:require [clojure.data.json :as json]))

(defn get-lotto-json [no]
  (json/read-str
   (slurp
    (str "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" no))))

(defn save-lotto []
  (->> (for [i (range 1 944)] (get-lotto-json i))
       (into [])
       (spit "lotto.json")))

;; 파일에서 모든 로또 번호 읽어오기
(def all-lotto-response (json/read-str (slurp "lotto.json") :key-fn keyword))

(reduce #(conj %1 (keyword (str "drwtNo" %2))) (into '() [:drwNo :bnusNo]) (range 6 0 -1))

;; json의 drwtNo를 가져오기 위한 구조를 반환
(defn- get-lotto-no-keywords []
  (reduce #(conj %1 (keyword (str "drwtNo" %2))) (into '() [:drwNo :bnusNo]) (range 6 0 -1)))

;; json 으로 되어 있는 값들을 [1 2 3 4 5 6 보너스, 회차]의 형태로 바꾸기
(def all-lotto
  (let [no (get-lotto-no-keywords)]
    (reduce #(conj %1 (vals (select-keys %2 no))) [] all-lotto-response)))

;; hash-map에서 value가 제일 큰 값 가져오기
(defn- get-big-number [coll]
  (reduce #(if (> (val (first %1)) (val (first %2))) %1 %2) (first coll) (rest coll)))

;; 큰 값을 가져오기
(defn- get-big-lotto [digit lotto-list]
  (->> (map #(nth % digit) lotto-list)
       (sort)
       (partition-by identity)
       (map #(hash-map (first %) (count %)))
       (get-big-number)))

;; 이 함수를 이용해서 어떻게 [1 2 3 4 5 6]의 형태를 
;; 만들 수 있는지 생각해보기
;; 자리수 마다 큰 수를 반환하는 함수
(defn get-big-lotto-digit []
  (->> (for [i (range 0 6)]
         (get-big-lotto i all-lotto))
       (map #(key (first %)))))

;; 모든 자리수에서 제일 적게 나온 숫자 가져오기
(defn get-small-values []
  (->> (reduce #(into %1 %2) [] all-lotto)
       (sort)
       (partition-by identity)
       (map #(hash-map (first %) (count %)))
       (into {})
       (sort #(< (last %1) (last %2)))
       (take 6)
       (sort #(< (first %1) (first %2)))))

;; 모든 자리수에서 제일 많이 나온 숫자 가져오기
(defn get-big-values []
  (->> (reduce #(into %1 %2) [] all-lotto)
       (sort)
       (partition-by identity)
       (map #(hash-map (first %) (count %)))
       (into {})
       (sort #(> (last %1) (last %2)))
       (take 6)
       (sort #(< (first %1) (first %2)))))

(defn- remain-numbers [coll]
  (remove (into #{} coll) (range 1 (inc 45))))

;; 자리수에서 한번도 나오지 않은 숫자 가져오기
;; [(1 2 3 4 5 6) (1 2 3 4 5 6)]
;; for를 하지 않고 수학적인 방법으로 자리수 마다 적게 나온 숫자를
;; 표시할 수는 없을까?
;; 중복을 제거하고 1의 자리에 대한 최소한의 값을 가져온다.
;; 나온적 없는 번호를 자리수 별로 리스트로 표시
(defn off-numbers [digit]
  (->> (map #(nth % (dec digit)) all-lotto)
       (distinct)
       (remain-numbers)
       (remove #{1 2 3 45})))

;; 자리수 마다 안나온 수 출력
(for [i (range 1 7)] 
  (off-numbers i))

;; 첫째 자리 가장 큰 수
(take 1 (sort #(> (first %1) (first %2)) all-lotto))

;; 2째자리 가장 큰 수
(take 1 (sort #(> (second %1) (second %2)) all-lotto))

;; 3째자리 가장 큰 수
(take 1 (sort #(> (nth %1 2) (nth %2 2)) all-lotto))

;; 자리수 마다 큰 수를 뽑아보기