(ns to-be-continued.fns)

(defn- assoc-result
  [result idx x]
  (-> result
      (assoc-in [:values idx] x)
      (update-in [:num-remaining] dec)))

(defn make-result-setter
  [result idx k]
  (fn [x]
    (let [new-result (swap! result assoc-result idx x)]
      (if (= (:num-remaining new-result) 0)
        (k (:values new-result))))))

(defn map-par
  [pf coll k]
  (let [pairs      (map vector coll (iterate inc 0))
        call-count (count pairs)
        result     (atom {:num-remaining call-count
                          :values        (into [] (repeat call-count nil))})]
    ;; Fire off the async calls
    (doseq [[x idx] pairs]
      (pf x (make-result-setter result idx k)))))

(defn forkv
  [k & pfs]
  (let [pairs      (map vector pfs (iterate inc 0))
        call-count (count pairs)
        result     (atom {:num-remaining call-count
                          :values        (into [] (repeat call-count nil))})]
    ;; Fire off the async calls
    (doseq [[pf idx] pairs]
      (pf (make-result-setter result idx k)))))

(defn error
  "Creates an error object having the provided tag"
  [tag]
  (with-meta {:type tag} {:error true}))

(defn error?
  [value]
  (boolean (:error (meta value))))