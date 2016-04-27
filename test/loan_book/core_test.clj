(ns loan-book.core-test
  (:require [clojure.test :refer :all]
            [loan-book.core :refer :all]))

(deftest test-create-lend
  (testing "create lend from account with sufficient funds."
    (let
      [account {:account-id "Bill", :balance 10000, :open-orders '()}
       [account' lend] (create-lend account 6000 6.5 10)]
    (is (=
      lend
      {:party "Bill", :side :lend, :principal 6000, :rate 6.5, :term 10, :leaves 6000}))
    (is (=
      account'
      {:account-id "Bill", :balance 4000, :open-orders (list lend)})))))

(deftest test-create-order
  (testing "create a borrow order"
    (is (=
      {:party "Bill", :principal 5000, :side :borrow, :rate 6.75, :term 10,
       :leaves 5000}
      (create-order "Bill" :borrow 5000 6.75 10))))
  (testing "create a lend order"
    (is (=
      {:party "Ben", :principal 1000, :side :lend, :rate 8.25, :term 10,
       :leaves 1000}
      (create-order "Ben" :lend 1000 8.25 10)))))

(deftest test-create-book
  (testing "create a book"
    (is (=
      {:term 10, :lends '(), :borrows '()}
      (create-book 10)))))

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
             :borrows '({:principal 1000, :leaves 1000, :side :borrow,
                         :rate 6.5})}
       [contracts book'] (cross 1 book)]
      (is (= '() contracts)
      (is (= book book')))))
  (testing "Cross a book with exact match at the margin."
    (let
      [book {:lends '({:principal 1000, :leaves 1000, :side :lend, :rate 6.5}
                      {:principal 1500, :leaves 1500, :side :lend, :rate 6.5}),
             :borrows '({:principal 1000, :leaves 1000, :side :borrow,
                         :rate 8.5}
                        {:principal 2000, :leaves 2000, :side :borrow,
                         :rate 8.5})}
       [contracts book'] (cross 2 book)]
      (is (= 
        {:lends '({:principal 1500, :leaves 1500, :side :lend, :rate 6.5}),
         :borrows '({:principal 2000, :leaves 2000, :side :borrow, :rate 8.5})}
        book'))
      (is (=
        '({:amount 1000,
           :borrow {:principal 1000, :leaves 0, :side :borrow, :rate 8.5},
           :lend {:principal 1000, :leaves 0, :side :lend, :rate 6.5}})
        contracts))))
  (testing "Cross a book with partial lend at the margin."
    (let
      [book {:lends '({:principal 1000, :leaves 500, :side :lend, :rate 6.5}
                      {:principal 1500, :leaves 1500, :side :lend, :rate 5.5}),
             :borrows '({:principal 1100, :leaves 1100, :side :borrow,
                         :rate 8.5}
                        {:principal 2000, :leaves 2000, :side :borrow,
                         :rate 8.5})}
       [contracts book'] (cross 2 book)]
      (is (= 
        {:lends '({:principal 1500, :leaves 1500, :side :lend, :rate 5.5}),
         :borrows '({:principal 1100, :leaves 600, :side :borrow, :rate 8.5}
                    {:principal 2000, :leaves 2000, :side :borrow, :rate 8.5})}
        book'))
      (is (=
        '({:amount 500,
           :borrow {:principal 1100, :leaves 600, :side :borrow, :rate 8.5},
           :lend {:principal 1000, :leaves 0, :side :lend, :rate 6.5}})
        contracts))))
  (testing "Cross a book with partial borrow at the margin."
    (let
      [book {:lends '({:principal 1000, :leaves 600, :side :lend, :rate 6.5}
                      {:principal 1500, :leaves 1500, :side :lend, :rate 5.5}),
             :borrows '({:principal 1100, :leaves 500, :side :borrow, :rate 8.5}
                        {:principal 2000, :leaves 2000, :side :borrow,
                         :rate 8.5})}
       [contracts book'] (cross 2 book)]
      (is (= 
        {:lends '({:principal 1000, :leaves 100, :side :lend, :rate 6.5}
                  {:principal 1500, :leaves 1500, :side :lend, :rate 5.5}),
         :borrows '({:principal 2000, :leaves 2000, :side :borrow, :rate 8.5})}
        book'))
      (is (=
        '({:amount 500,
           :borrow {:principal 1100, :leaves 0, :side :borrow, :rate 8.5},
           :lend {:principal 1000, :leaves 100, :side :lend, :rate 6.5}})
        contracts)))))

(deftest test-contract-between
  (testing "valid contract"
    (is (=
      {:amount 500,
       :lend {:principal 700, :leaves 0, :side :lend, :rate 6.5},
       :borrow {:principal 2000, :leaves 1500, :side :borrow, :rate 8.5}}
      (contract-between
        500
       {:principal 2000, :leaves 2000, :side :borrow, :rate 8.5}
       {:principal 700, :leaves 500, :side :lend, :rate 6.5})))))
