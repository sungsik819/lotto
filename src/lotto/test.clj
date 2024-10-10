(ns lotto.test

  (:require [clojure.test :refer [deftest is testing]]
            [lotto.core :refer [file-data lotto-data group-by-number
                                get-digit-count first-d lotto-colors
                                unincluded-numbers]]))


(def dummy-data
  [{:firstWinamnt 0, :drwtNo4 33, :totSellamnt 3681782000, :firstAccumamnt 863604600, :returnValue "success", :drwtNo2 23, :drwtNo5 37, :drwtNo3 29, :firstPrzwnerCo 0, :bnusNo 16, :drwNoDate "2002-12-07", :drwtNo6 40, :drwtNo1 10, :drwNo 1}
   {:firstWinamnt 2002006800, :drwtNo4 25, :totSellamnt 4904274000, :firstAccumamnt 0, :returnValue "success", :drwtNo2 13, :drwtNo5 32, :drwtNo3 21, :firstPrzwnerCo 1, :bnusNo 2, :drwNoDate "2002-12-14", :drwtNo6 42, :drwtNo1 9, :drwNo 2}])

(deftest test-test
  (with-redefs [file-data dummy-data]
    (testing "[1 2 3 4 5 6 보너스] 형태 확인"
      (is (= (lotto-data)
             [[10 23 29 33 37 40 16] [9 13 21 25 32 42 2]])))
    (testing "전체 자릿수에 대한 [숫자 횟수] 형태 확인"
      (is (= ((group-by-number (flatten (lotto-data))) 0)
             [21 1])))
    (testing "첫째 자릿수에 대한 [숫자 횟수] 형태 확인"
      (is (= (get-digit-count first-d)
             [[10 1] [9 1]])))
    (testing "전체 자릿수에 대한 [색이름 횟수] 형태 확인"
      (is (= ((group-by-number (flatten (lotto-colors))) 0)
             [:yellow 3])))
    (testing "로또 번호에서 안나온 숫자 확인"
      (is (= (unincluded-numbers (flatten (lotto-data)))
             [1 3 4 5 6 7 8 11 12 14 15 17 18 19 20 22 24 26 27 28 30 31 34 35 36 38 39 41 43 44 45])))))

(clojure.test/run-tests 'lotto.test)