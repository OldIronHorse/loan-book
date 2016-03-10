(ns loan-book.core-test
  (:require [clojure.test :refer :all]
            [loan-book.core :refer :all]))

(deftest test-add-order
  (testing "Add order to an empty book"
    (is (=
      {:borrows '(),
       :lends '({:principal 10000, :side :lend, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principal 10000, :side :lend, :rate 6})))
    (is (=
      {:lends '(),
       :borrows '({:principal 10000, :side :borrow, :rate 6})}
      (add-order
        {:borrows '(), :lends '()}
        {:principal 10000, :side :borrow, :rate 6}))))
  (testing "Add an order to the top of the book"
    (is (=
      {:borrows '(),
       :lends '({:principal 10000, :side :lend, :rate 6}
                {:principal 1000, :side :lend, :rate 6.5})}
      (add-order
        {:borrows '(),
         :lends '({:principal 1000, :side :lend, :rate 6.5})}
        {:principal 10000, :side :lend, :rate 6})))
    (is (=
      {:lends '(),
       :borrows '({:principal 10000, :side :borrow, :rate 7}
                  {:principal 1000, :side :borrow, :rate 6.5})}
      (add-order
        {:lends '(),
         :borrows '({:principal 1000, :side :borrow, :rate 6.5})}
        {:principal 10000, :side :borrow, :rate 7}))))
  (testing "Add an order to the bottom of a book"
    (is (=
      {:borrows '(),
       :lends '({:principal 1000, :side :lend, :rate 6.5}
                {:principal 10000, :side :lend, :rate 7})}
      (add-order
        {:borrows '(),
         :lends '({:principal 1000, :side :lend, :rate 6.5})}
        {:principal 10000, :side :lend, :rate 7})))
    (is (=
      {:lends '(),
       :borrows '({:principal 1000, :side :borrow, :rate 6.5}
                  {:principal 10000, :side :borrow, :rate 6})}
      (add-order
        {:lends '(),
         :borrows '({:principal 1000, :side :borrow, :rate 6.5})}
        {:principal 10000, :side :borrow, :rate 6}))))
  (testing "Add an order to the middle of the book"
    (is (=
      {:borrows '(),
       :lends '({:principal 1000, :side :lend, :rate 6.5}
                {:principal 10000, :side :lend, :rate 7}
                {:principal 2000, :side :lend, :rate 7.5})}
      (add-order
        {:borrows '(),
         :lends '({:principal 1000, :side :lend, :rate 6.5}
                  {:principal 2000, :side :lend, :rate 7.5})}
        {:principal 10000, :side :lend, :rate 7})))
    (is (=
      {:lends '(),
       :borrows '({:principal 1000, :side :borrow, :rate 7.5}
                  {:principal 10000, :side :borrow, :rate 7}
                  {:principal 2000, :side :borrow, :rate 6.5})}
      (add-order
        {:lends '(),
         :borrows '({:principal 1000, :side :borrow, :rate 7.5}
                    {:principal 2000, :side :borrow, :rate 6.5})}
        {:principal 10000, :side :borrow, :rate 7})))))

(deftest test-cross
  (testing "Cross an empty book"
    (let
      [book {:lends '(), :borrows '()}
       [contracts book'] (cross 1 book)]
      (is (= '() contracts))
      (is (= book book'))))
  (testing "Cross a single-sided book (borrow)."
    (let
      [book {:borrows '({:principal 1000, :side :borrow, :rate 6.5}),
             :lends '()}
       [contracts book'] (cross 1 book)]
      (is (= '() contracts))
      (is (= book book'))))
  (testing "Cross a single-sided book (lend)."
    (let
      [book {:lends '({:principal 1000, :side :lend, :rate 6.5}),
             :borrows '()}
       [contracts book'] (cross 1 book)]
      (is (= '() contracts))
      (is (= book book'))))
  (testing "Cross a book with orders outside the margin."
    (let
      [book {:lends '({:principal 1000, :leaves 1000, :side :lend, :rate 6.5}),
             :borrows '({:principal 1000, :leaves 1000, :side :borrow, :rate 6.5})}
       [contracts book'] (cross 1 book)]
      (is (= '() contracts)
      (is (= book book')))))
  (testing "Cross a book with exact match at the margin."
    (let
      [book {:lends '({:principal 1000, :leaves 1000, :side :lend, :rate 6.5}
                      {:principal 1500, :leaves 1500, :side :lend, :rate 6.5}),
             :borrows '({:principal 1000, :leaves 1000, :side :borrow, :rate 8.5}
                        {:principal 2000, :leaves 2000, :side :borrow, :rate 8.5})}
       [contracts book'] (cross 2 book)]
      (is (= 
        {:lends '({:principal 1500, :leaves 1500, :side :lend, :rate 6.5}),
         :borrows '( {:principal 2000, :leaves 2000, :side :borrow, :rate 8.5})}
        book'))
      (is (= '({:amount 1000,
                :borrow {:principal 1000, :leaves 0, :side :borrow, :rate 8.5},
                :lend {:principal 1000, :leaves 0, :side :lend, :rate 6.5}}))))))
