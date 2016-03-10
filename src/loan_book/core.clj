(ns loan-book.core)

(defn create-order
  [party side principal rate term]
  {:party party, :side side, :principal principal, :rate rate, :term term,
   :leaves principal})

(defn add-order
  "Add a loan order to the specified order book. Return the updated book."
  [book, order]
  (case (:side order)
    :borrow (update book :borrows #(sort-by :rate > (cons order %)))
    :lend (update book :lends #(sort-by :rate (cons order %)))))

(defn contract-between
  [amount borrow lend]
  (let
    [borrow' (update borrow :leaves #(- % amount))
     lend' (update lend :leaves #(- % amount))]
    {:amount amount, :borrow borrow', :lend lend'}))

(defn cross
  "Cross an orderbook at the specified margin."
  [margin, book]
  (let
    [borrow (first (:borrows book))
     lend (first (:lends book))]
    (if
      (and
        (not (nil? borrow))
        (not (nil? lend))
        (<= margin (- (:rate borrow) (:rate lend))))
      (let
        [contract
          (contract-between (min (:leaves borrow) (:leaves lend)) borrow lend)
         book' 
          (update
            (update book :borrows 
              #(filter (fn [b] (pos? (:leaves b))) 
              (cons (:borrow contract) (rest %))))
            :lends #(filter (fn [l] (pos? (:leaves l))) 
            (cons (:lend contract) (rest %))))]
        (list (list contract) book'))
      (list '(), book))))
