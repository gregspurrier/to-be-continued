(ns to-be-continued.macros
  "Asynchronous-aware equivalents of Clojure's ->, ->>, and let macros")

;; NOTE: In order to support ClojureScript, the functions defined in
;; this namespace may only used at the time of macro expansion. Any
;; functions that must be available at run time--i.e., functions
;; referenced in the expanded code--must be defined in the
;; to-be-continued.fns namespace.

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

(defn- intermediate-form
  "Construct the form for an intermediate step in the thread"
  [x form remaining-forms thread-op arg-insert-fn]
  (let [form (promote-to-list form)]
    (if (requires-continuation? form)
      ;; Expand ... into a continuation
      `(if (to-be-continued.fns/error? ~x)
         (~thread-op ~x ~@remaining-forms)
         ~(-> form
              butlast
              (arg-insert-fn x)
              (add-continuation `(fn [result#] 
                                   (~thread-op result# ~@remaining-forms)))))
      ;; Synchronous case
      `(if (to-be-continued.fns/error? ~x) 
         (~thread-op ~x ~@remaining-forms)
         (~thread-op ~(arg-insert-fn form x) ~@remaining-forms)))))

(defn- final-thread-form
  "Construct the form that executes the final step in the thread"
  [x form arg-insert-fn]
  (-> form
      promote-to-list
      (arg-insert-fn x)
      (execute-ignoring-result)))

(defmacro -+->
  "An asynchronous-aware equivalent of clojure.core/->. Threads expr
through the intermediate forms as their first argument and then
invokes the callback with the final result. Always returns nil.

If the intermediate form is asynchronous, indicated by its last
argument being '...', the '...' is replaced with an automatically
generated callback function that resumes processing of the thread once
the result of the asynchronous computation is available."
  {:arglists '([expr intermediate-forms* callback])}
  ([x form]
     (final-thread-form x form add-argument-first))
  ([x form & more]
     (intermediate-form x form more '-+-> add-argument-first)))

(defmacro -+->>
  "An asynchronous-aware equivalent of clojure.core/->>. Threads expr
through the intermediate forms as their last non-callback argument and
then invokes the callback with the final result. Always returns nil.

If the intermediate form is asynchronous, indicated by its last
argument being '...', the '...' is replaced with an automatically
generated callback function that resumes processing of the thread once
the result of the asynchronous computation is available."
  {:arglists '([expr intermediate-forms* callback])}
  ([x form]
     (final-thread-form x form add-argument-last))
  ([x form & more]
     (intermediate-form x form more '-+->> add-argument-last)))

(defn- executable-bound-form
  [form result-sym index k-sym]
  (if (and (list? form) (requires-continuation? form))
    `(~@(butlast form) (to-be-continued.fns/make-result-setter ~result-sym
                                                               ~index
                                                               ~k-sym))
    `((to-be-continued.fns/make-result-setter ~result-sym ~index ~k-sym)
      ~form)))

(defmacro let-par
  "An asynchronous-aware equivalent of clojure.core/let. If any of
the bound expressions in bindings is an asynchronous call, indicated
by its last argument being '...', the '...' is replaced with an
automatically-generated callback that will receive its result. Once
all asynchronous results have been realized, the body forms are
executed in an environment having the bindings in place."
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
              (iterate inc 0))
       nil)))