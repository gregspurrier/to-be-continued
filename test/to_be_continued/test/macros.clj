(ns to-be-continued.test.macros
  (:use to-be-continued.macros
        midje.sweet))

;; Helper functions used verifying threading behavior
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