(ns bridge.data.coll)

(defn index-of-pred
  "Return index of first element for which `(pred element)`
  returns true in `xs`, `nil` otherwise"
  [pred xs]
  (loop [tail xs
         idx  0]
    (cond
      (empty? tail) nil
      (pred (first tail)) idx
      :else (recur (next tail) (inc idx)))))

(defn index-of
  "Return index of first element in `xs`, `nil` otherwise"
  [val xs]
  (index-of-pred #(= val %) xs))
