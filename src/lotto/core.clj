(ns lotto.core
  (:require [clojure.data.json :as json]))

;; 구현 순서
;; 데이터 읽기 
;; 데이터 가공 하기
;; 가공된 데이터를 처리 하기

;; 색 정의
;; 1 ~ 10 : yellow
;; 11 ~ 20 : blue
;; 21 ~ 30 : red
;; 31 ~ 40 : gray
;; 41 ~ 45 : green

(defn get-data [round]
  (let [data (json/read-str
              (slurp
               (str "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" round))
              :key-fn keyword)]
    (if (= (:returnValue data) "success")
      data
      (throw (ex-info (str round " 회차 정보를 불러 올 수 없습니다.") data)))))

;; 전체 데이터에서 마지막 회차의 회차번호 가져오기
(def last-drwNo #(:drwNo (last %)))

;; 마지막 회차 저장 하기
(defn add-last-round []
  (let [data (read-string (slurp "lotto.edn"))
        round (inc (last-drwNo data))
        last-data (get-data round)]
    (->> (conj data last-data)
         (spit "lotto.edn"))))

;; 원격에서 전체 회차 데이터 가져오기
;; (def remote-data (map #(get-data %) (range 1 (inc last-round))))

;; 파일에 있는 정보 읽기
(def file-data (read-string (slurp "lotto.edn")))

;; 회차 데이터 가져오기
(def ^:private get-round #(nth file-data (dec %)))

;; 범위에 있는 데이터 가져오기
(defn ^:private get-rounds [start end]
  (map get-round (range start (inc end))))

;; [1 2 3 4 5 6 보너스]의 형태로 변환
(defn trans-data [& coll]
  (let [{:keys [drwtNo1 drwtNo2 drwtNo3 drwtNo4 drwtNo5 drwtNo6 bnusNo]} coll]
    [drwtNo1 drwtNo2 drwtNo3 drwtNo4 drwtNo5 drwtNo6 bnusNo]))

;; 로또 데이터로 변환
(defn lotto-data
  ([]
   (lotto-data file-data))
  ([file-data]
   (map trans-data file-data))
  ([start end] ;; 범위에 있는 회차 가져오기
   (->> (get-rounds start end)
        (lotto-data))))

;; 범위의 숫자에 해당하는 값 리턴
;; 교체하는 로직만 있으면 될 것 같아서 데이터 구조는 없음
(defn get-color [n]
  (cond
    (contains? (set (range 1 11)) n) :yellow
    (contains? (set (range 11 21)) n) :blue
    (contains? (set (range 21 31)) n) :red
    (contains? (set (range 31 41)) n) :gray
    :else :green))

;; 한줄을 색깔명으로 교체
(map get-color [2 10 12 15 22 44 1])


;; 위 데이터를 바탕으로 적게 나온 숫자 가져오기
;; [숫자 횟수] 형태로 바꾼다.
(def group-by-number #(vec (frequencies %)))

;; 오름 차순
(def sort-asc #(sort-by second %))

;; 내림 차순
(def sort-desc #(sort-by second > %))

;; 자리에 있는 데이터 가져오는 함수
(def first-d #(first %))
(def second-d #(second %))
(def third-d #(second-d (rest %)))
(def forth-d #(third-d (rest %)))
(def fifth-d #(forth-d (rest %)))
(def sixth-d #(fifth-d (rest %)))
(def bonus-d #(last %))

;; 1~45번까지 로또 번호 리스트
(def lotto-numbers (range 1 (inc 45)))

;; 나오지 않은 로또 숫자 찾는 함수
(def unincluded-numbers #(filter (complement (set %)) lotto-numbers))

;; [숫자 횟수] 리스트에서 숫자만 n개 가져오기
(defn get-numbers [n number-counts]
  (map first (sort (take n number-counts))))

;; 자리수에 대한 통계
(defn digit-statistics [digit all-data]
  (let [digit-data (map digit all-data) ;; 자리에 있는 데이터
        number-counts (group-by-number digit-data) ;; [숫자 횟수] 그룹화 데이터
        min-numbers (sort-asc number-counts) ;; 오름차순 데이터
        max-numbers (sort-desc number-counts)] ;; 내림 차순 데이터
    {:min (get-numbers 7 min-numbers) ;; 가장 적게 나온 숫자 7개
     :max (get-numbers 7 max-numbers) ;; 가장 많이 나온 숫자 7개
     :unincluded (unincluded-numbers digit-data)}))

;; 자리에 대한 각각 통계 데이터
(defn statistics
  ([]
   (statistics lotto-data))
  ([lotto-data]
   (map #(digit-statistics % lotto-data) [first-d second-d third-d forth-d fifth-d sixth-d bonus-d])))

;; 숫자를 색으로 교체
(defn lotto-colors
  ([]
   (map #(map get-color %) (lotto-data)))
  ([start end]
   (map #(map get-color %) (lotto-data start end))))

;; 자리에 해당하는 [숫자 횟수] 형태의 데이터 가져오기
(defn get-digit-count [df]
  (let [d (map df (lotto-data))]
    (group-by-number d)))

;; 위 함수를 만들기 위한 테스트
(comment
  ;; 첫째 자리수 가져오기
  (def first-digit (map first (lotto-data)))
;; [숫자 횟수] 형태로 변환
  (def first-number-counts (group-by-number first-digit))

;; 첫째 자리에서 안나온 숫자 확인
  (unincluded-numbers first-digit)

;; 둘째 자리수 가져오기
  (def second-digit (map second (lotto-data)))

;; 둘째 자리에서 안나온 숫자 확인
  (unincluded-numbers second-digit)

;; 셋째 자리수 가져오기
  (def third-digit (map third-d (lotto-data)))

;; 셋째 자리에서 안나온 숫자 확인
  (unincluded-numbers third-digit)

;; 넷째 자리수 가져오기
  (def forth-digit (map forth-d (lotto-data)))

;; 넷째 자리에서 안나온 숫자 확인
  (unincluded-numbers forth-digit)

;; 다섯째 자리수 가져오기
  (def fifth-digit (map fifth-d (lotto-data)))

;; 다섯째 자리에서 안나온 숫자 확인
  (unincluded-numbers fifth-digit)

;; 여섯째 자리수 가져오기
  (def sixth-digit (map sixth-d (lotto-data)))

;; 여섯째 자리에서 안나온 숫자 확인
  (unincluded-numbers sixth-digit)

;; 보너스 숫자만 가져오기
  (def bonus-digit (map bonus-d (lotto-data)))

;; 보너스 자리에서 안나온 숫자 확인 - 없음
  (unincluded-numbers bonus-digit))