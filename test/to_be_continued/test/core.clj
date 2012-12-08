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

(fact "threading macros can be nested"
  (-+-> ..value..
        (-+-> (async-one-arg-fn ...) ...)
        (-+->> (async-one-arg-fn ...) ...)
        callback)
  => nil
  (provided (one-arg-fn ..value..) => ..value2..
            (one-arg-fn ..value2..) => ..final-value..
            (callback ..final-value..) => ..anything..))

(facts "about let-par"
  (fact "behaves like let for synchronous forms"
    (let-par [a ..value.., b (identity ..another-value..)]
             (callback {:a a, :b b}))
    => nil
    (provided (callback {:a ..value.., :b ..another-value..}) => ..anything..))
  (fact "replaces ... with a continuation and waits for result"
    (let-par [a (async-one-arg-fn ..an-arg.. ...)
              b (async-two-arg-fn ..arg1.. ..arg2.. ...)]
             (callback {:a a, :b b}))
    => nil
    (provided (one-arg-fn ..an-arg..) => ..one-arg-result..
              (two-arg-fn ..arg1.. ..arg2..) => ..two-arg-result..
              (callback {:a ..one-arg-result.., :b ..two-arg-result..}) 
              => ..anything..))
  (fact "supports ... at the end of a thread expression"
    (let-par [a (-+-> ..an-arg.. (async-one-arg-fn ...) ...)]
             (callback a))
    => nil
    (provided (one-arg-fn ..an-arg..) => ..one-arg-result..
              (callback ..one-arg-result..) => ..anything..))
  (fact "allows synchronous and asynchronous bindings to be mixed"
    (let-par [a ..value.., b (async-one-arg-fn ..arg.. ...)]
             (callback {:a a, :b b}))
    => nil
    (provided (one-arg-fn ..arg..) => ..result..
              (callback {:a ..value.., :b ..result..}) => ..anything..)))

(facts "about error handling in -+->"
  (let [an-error (error :boom)]
    (fact "errors short-circuit remaining intermediate forms"
      (-+-> ..value..
            one-arg-fn
            (two-arg-fn ..other-arg..)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+-> ..value..
            one-arg-fn
            (async-two-arg-fn ..other-arg.. ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+-> ..value..
            (async-one-arg-fn ...)
            (two-arg-fn ..other-arg..)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+-> ..value..
            (async-one-arg-fn ...)
            (async-two-arg-fn ..other-arg.. ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..))
    (fact "error in final intermediate form still invokes callback"
      (-+-> ..value..
            one-arg-fn
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+-> ..value..
            (async-one-arg-fn ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..))))

(facts "about error handling in -+->>"
  (let [an-error (error :boom)]
    (fact "errors short-circuit remaining intermediate forms"
      (-+->> ..value..
            one-arg-fn
            (two-arg-fn ..other-arg..)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+->> ..value..
            one-arg-fn
            (async-two-arg-fn ..other-arg.. ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+->> ..value..
            (async-one-arg-fn ...)
            (two-arg-fn ..other-arg..)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+->> ..value..
            (async-one-arg-fn ...)
            (async-two-arg-fn ..other-arg.. ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..))
    (fact "error in final intermediate form still invokes callback"
      (-+->> ..value..
            one-arg-fn
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..)

      (-+->> ..value..
            (async-one-arg-fn ...)
            callback)
      => nil
      (provided (one-arg-fn ..value..) => an-error
                (callback an-error) => ..anything..))))