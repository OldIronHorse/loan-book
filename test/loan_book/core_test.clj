(ns loan-book.core-test
  (:require [clojure.test :refer :all]
            [loan-book.core :refer :all]))

(deftest test-add-order
  (testing "Add order to an empty book"
    (is (=
      {:borrows '(),
       :lends '({:principle 10000, :side :lend, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principle 10000, :side :lend, :rate 6})))
    (is (=
      {:lends '(),
       :borrows '({:principle 10000, :side :borrow, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principle 10000, :side :borrow, :rate 6}))))
  (testing "Add an order to the top of the book"
    (is (=
      {:borrows '(),
       :lends '({:principle 10000, :side :lend, :rate 6}
                {:principle 1000, :side :lend, :rate 6.5})}
      (add-order
        {:borrows '(),
         :lends '({:principle 1000, :side :lend, :rate 6.5})}
        {:principle 10000, :side :lend, :rate 6})))
    (is (=
      {:lends '(),
       :borrows '({:principle 10000, :side :borrow, :rate 7}
                  {:principle 1000, :side :borrow, :rate 6.5})}
      (add-order
        {:lends '(),
         :borrows '({:principle 1000, :side :borrow, :rate 6.5})}
        {:principle 10000, :side :borrow, :rate 7}))))
  (testing "Add an order to the bottom of a book"
    (is (=
      {:borrows '(),
       :lends '({:principle 1000, :side :lend, :rate 6.5}
                {:principle 10000, :side :lend, :rate 7})}
      (add-order
        {:borrows '(),
         :lends '({:principle 1000, :side :lend, :rate 6.5})}
        {:principle 10000, :side :lend, :rate 7})))
    (is (=
      {:lends '(),
       :borrows '({:principle 1000, :side :borrow, :rate 6.5}
                  {:principle 10000, :side :borrow, :rate 6})}
      (add-order
        {:lends '(),
         :borrows '({:principle 1000, :side :borrow, :rate 6.5})}
        {:principle 10000, :side :borrow, :rate 6}))))
  (testing "Add an order to the middle of the book"
    (is (=
      {:borrows '(),
       :lends '({:principle 1000, :side :lend, :rate 6.5}
                {:principle 10000, :side :lend, :rate 7}
                {:principle 2000, :side :lend, :rate 7.5})}
      (add-order
        {:borrows '(),
         :lends '({:principle 1000, :side :lend, :rate 6.5}
                  {:principle 2000, :side :lend, :rate 7.5})}
        {:principle 10000, :side :lend, :rate 7})))
    (is (=
      {:lends '(),
       :borrows '({:principle 1000, :side :borrow, :rate 7.5}
                  {:principle 10000, :side :borrow, :rate 7}
                  {:principle 2000, :side :borrow, :rate 6.5})}
      (add-order
        {:lends '(),
         :borrows '({:principle 1000, :side :borrow, :rate 7.5}
                    {:principle 2000, :side :borrow, :rate 6.5})}
        {:principle 10000, :side :borrow, :rate 7})))))

(deftest test-cross
  (testing "Cross an empty book"
    (is (=
      {:contracts '(), :book {:lends '(), :borrows '()}}
      (cross 1 {:lends '(), :borrows '()}))))
  (testing "Cross a single-sdied book (borrow)."
    (let
      [book {:borrows '({:principle 1000, :side :borrow, :rate 6.5}),
             :lends '()}]
      (is (= {:contracts '(), :book book} (cross 1 book)))))
  (testing "Cross a single-sdied book (lend)."
    (let
      [book {:lends '({:principle 1000, :side :lend, :rate 6.5}),
             :borrows '()}]
      (is (= {:contracts '(), :book book} (cross 1 book)))))
  (testing "Cross a book with orders outside the margin."
    (let
      [book {:lends '({:principle 1000, :side :lend, :rate 6.5}),
             :borrows '({:principle 1000, :side :borrow, :rate 4.5})}]
      (is (= {:contracts '(), :book book} (cross 1 book))))))
