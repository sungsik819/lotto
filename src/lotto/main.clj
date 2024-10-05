(ns lotto.main
  (:require [lotto.core :as lotto]))

;; 신규 마지막 회차 저장 하기
(lotto/add-last-round)

;; 전체 자리수에 있는 숫자의 횟수 계산
(def all-number-counts (lotto/group-by-number (mapcat identity (lotto/lotto-data))))

;; 숫자가 적게 나온 번호 순으로
(def min-numbers (lotto/sort-asc all-number-counts))

;; 숫자가 많이 나온 번호 순으로
(def max-numbers (lotto/sort-desc all-number-counts))


;; 숫자가 적게 나온 로또 번호 가져오기(보너스 번호 포함)
;; [1 2 3 4 5 6 보너스]
(lotto/get-numbers 7 min-numbers)

;; 숫자가 많이 나온 로또 번호 가져오기(보너스 번호 포함)
;; [1 2 3 4 5 6 보너스]
(lotto/get-numbers 7 max-numbers)


;; 마지막 10개 회차만 보기
(drop 1130 (lotto/lotto-data))

(lotto/statistics)

;; 1 ~ 100까지 회차에서의 통계 보기
(->> (lotto/lotto-data 1 100)
     (lotto/statistics))

;; 전체 색 데이터가 나타난 횟수 확인
(lotto/group-by-number (mapcat identity (lotto/lotto-colors)))

;; 1~100회차에 대해 색이 나타난 횟수 확인
(lotto/group-by-number (mapcat identity (lotto/lotto-colors 1 100)))