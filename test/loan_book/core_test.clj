(ns loan-book.core-test
  (:require [clojure.test :refer :all]
            [loan-book.core :refer :all]))

(deftest test-add-order
  (testing "Add order to an empty book"
    (is (=
      {:borrows '(),
       :lends '({:principle 10000, :side :lend, :term 10, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principle 10000, :side :lend, :term 10, :rate 6})))
    (is (=
      {:lends '(),
       :borrows '({:principle 10000, :side :borrow, :term 10, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principle 10000, :side :borrow, :term 10, :rate 6})))))
