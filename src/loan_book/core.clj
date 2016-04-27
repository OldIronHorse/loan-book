(ns loan-book.core)

(defn create-order
  "Create a new order"
  [party side principal rate term]
  {:party party, :side side, :principal principal, :rate rate, :term term,
   :leaves principal})

(defn create-lend
  [lend-account size rate term]
  (let
    [order (create-order (:account-id lend-account) :lend size rate term)
     lend-account' (update
                    (update
                      lend-account
                      :balance
                      #(- % size))
                    :open-orders
                    #(cons order %))]
    (list lend-account' order)))

(defn create-book
  "Create a new, empty book."
  [term]
  {:term term, :lends '(), :borrows '()})

(defn add-order
  "Add a loan order to the specified order book. Return the updated book."
  [book, order]
  (case (:side order)
    :borrow (update book :borrows #(sort-by :rate > (cons order %)))
    :lend (update book :lends #(sort-by :rate (cons order %)))))

(defn contract-between
  "Create a contract for the specified amount between a lend/borrow pair."
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
