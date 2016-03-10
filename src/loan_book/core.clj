(ns loan-book.core)

(defn add-order
  "Add a loan order to the specified order book. Return the updated book."
  [book, order]
  (case (:side order)
    :borrow (update book :borrows #(sort-by :rate > (cons order %)))
    :lend (update book :lends #(sort-by :rate (cons order %)))))

(defn cross
  "Cross an orderbook at the specified margin."
  [margin, book]
  {:contracts '(), :book book})
