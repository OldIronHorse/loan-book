(ns loan-book.core)

(defn add-order
  "Add a loan order to the specified order book. Return the updated book."
  [book, order]
  (case (:side order)
    :borrow (update book :borrows #(cons order %))
    :lend (update book :lends #(cons order %))))
