(ns to-be-continued.test.core
  (:use to-be-continued.core
        midje.sweet))

;; Helper functions used in the verification of threading behavior
(unfinished one-arg-fn)
(unfinished two-arg-fn)
(unfinished callback)

(defn async-one-arg-fn
  [arg k]
  (k (one-arg-fn arg)))

(defn async-two-arg-fn
  [arg1 arg2 k]
  (k (two-arg-fn arg1 arg2)))

(facts "about -+->"
  (fact "behaves like -> and invokes callback with result"
    (-+-> ..value..
          (one-arg-fn)
          (two-arg-fn ..second-arg..)
          callback)
    => nil
    (provided (one-arg-fn ..value..) => ..value2..
              (two-arg-fn ..value2.. ..second-arg..) => ..final-value..
              (callback ..final-value..) => ..anything..))

  (fact "replaces ... with a continuation of the thread"
    (-+-> ..value..
          (async-one-arg-fn ...)
          (async-two-arg-fn ..second-arg.. ...)
          callback)
    => nil
    (provided (one-arg-fn ..value..) => ..value2..
              (two-arg-fn ..value2.. ..second-arg..) => ..final-value..
              (callback ..final-value..) => ..anything..))

  (fact "synchronous and asynchronous functions can be mixed in the thread"
    (-+-> ..value..
          (two-arg-fn ..some-arg..)
          (async-one-arg-fn ...)
          (async-two-arg-fn ..another-arg.. ...)
          (one-arg-fn)
          callback)
    => nil
    (provided (two-arg-fn ..value.. ..some-arg..) => ..value2..
              (one-arg-fn ..value2..) => ..value3..
              (two-arg-fn ..value3.. ..another-arg..) => ..value4..
              (one-arg-fn ..value4..) => ..final-value..
              (callback ..final-value..) => ..anything..)))

(facts "about -+->>"
  (fact "behaves like ->> and invokes callback with result"
    (-+->> ..value..
           (one-arg-fn)
           (two-arg-fn ..first-arg..)
           callback)
    => nil
    (provided (one-arg-fn ..value..) => ..value2..
              (two-arg-fn ..first-arg.. ..value2..) => ..final-value..
              (callback ..final-value..) => ..anything..))

  (fact "replaces ... with a continuation of the thread"
    (-+->> ..value..
           (async-one-arg-fn ...)
           (async-two-arg-fn ..first-arg.. ...)
           callback)
    => nil
    (provided (one-arg-fn ..value..) => ..value2..
              (two-arg-fn ..first-arg.. ..value2..) => ..final-value..
              (callback ..final-value..) => ..anything..))

  (fact "synchronous and asynchronous functions can be mixed in the thread"
    (-+->> ..value..
           (two-arg-fn ..some-arg..)
           (async-one-arg-fn ...)
           (async-two-arg-fn ..another-arg.. ...)
           (one-arg-fn)
           callback)
    => nil
    (provided (two-arg-fn ..some-arg.. ..value..) => ..value2..
              (one-arg-fn ..value2..) => ..value3..
              (two-arg-fn ..another-arg.. ..value3..) => ..value4..
              (one-arg-fn ..value4..) => ..final-value..
              (callback ..final-value..) => ..anything..)))

(facts "about let-par"
  (fact "behaves like let for synchronous forms"
    (let-par [a ..value.., b (identity ..another-value..)]
             {:a a, :b b})
    => {:a ..value.., :b ..another-value..})
  (fact "replaces ... with a continuation and waits for result"
    (let-par [a (async-one-arg-fn ..an-arg.. ...)
              b (async-two-arg-fn ..arg1.. ..arg2.. ...)]
             {:a a, :b b})
    => {:a ..one-arg-result.., :b ..two-arg-result..}
    (provided (one-arg-fn ..an-arg..) => ..one-arg-result..
              (two-arg-fn ..arg1.. ..arg2..) => ..two-arg-result..))
  (fact "allows synchronous and asynchronous bindings to be mixed"
    (let-par [a ..value.., b (async-one-arg-fn ..arg.. ...)]
             {:a a, :b b})
    => {:a ..value.., :b ..result..}
    (provided (one-arg-fn ..arg..) => ..result..)))