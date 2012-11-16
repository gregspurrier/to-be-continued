(ns to-be-continued.macros)

(defn- promote-to-list
  [form]
  (if (list? form)
    form
    (list form)))

(defn- add-argument-first
  [[head & tail] arg]
  `(~head ~arg ~@tail))

(defn- add-argument-last
  [form arg]
  `(~@form ~arg))

(defn- execute-ignoring-result
  [form]
  `(do ~form nil))

(def ^:private add-continuation add-argument-last)

(defn- requires-continuation?
  [form]
  (= (last form) '...))

(defmacro -+->
  ([x form]
     (-> form
         promote-to-list
         (add-argument-first x)
         (execute-ignoring-result)))
  ([x form & more]
     (let [form (promote-to-list form)]
       (if (requires-continuation? form)
         ;; Build a continuation
         (-> form
             butlast
             (add-argument-first x)
             (add-continuation `(fn [result#] (-+-> result# ~@more))))
         ;; Same as normal threading
         `(-+-> ~(add-argument-first form x) ~@more)))))

(defmacro -+->>
  ([x form]
     (-> form
         promote-to-list
         (add-argument-last x)
         (execute-ignoring-result)))
  ([x form & more]
     (let [form (promote-to-list form)]
       (if (requires-continuation? form)
         ;; Build a continuation
         (-> form
             butlast
             (add-argument-last x)
             (add-continuation `(fn [result#] (-+->> result# ~@more))))
         ;; Same as normal threading
         `(-+->> ~(add-argument-last form x) ~@more)))))

(defn- executable-bound-form
  [form result-sym index k-sym]
  (if (requires-continuation? form)
    `(~@(butlast form) (to-be-continued.fns/make-result-setter ~result-sym
                                                               ~index
                                                               ~k-sym))
    `((make-result-setter ~result-sym ~index ~k-sym) ~form)))

(defmacro let-par
  [bindings & forms]
  (let [binding-pairs (partition 2 bindings)
        bound-vars (map first binding-pairs)
        bound-forms (map second binding-pairs)
        inner-k-sym (gensym)
        result-sym (gensym)]
    `(let [~result-sym (atom {:num-remaining ~(count bound-vars)
                          :values ~(into [] (repeat (count bound-vars) nil))})
           ~inner-k-sym (fn [result-vec#]
                          (apply (fn [~@bound-vars] ~@forms) result-vec#))]
       ~@(map #(executable-bound-form %1 result-sym %2 inner-k-sym)
              bound-forms
              (iterate inc 0)))))