(ns to-be-continued.core
  (:require to-be-continued.fns
            to-be-continued.macros))

;; This is a convenience namespace for blending the separate
;; to-be-continued.fns and to-be-continued.macros namespaces required
;; for ClojureScript into a single namespace for use in Clojure.

(def map-par to-be-continued.fns/map-par)
(def error to-be-continued.fns/error)

(defmacro -+->
  [expr & forms]
  `(to-be-continued.macros/-+-> ~expr ~@forms))

(defmacro -+->>
  [expr & forms]
  `(to-be-continued.macros/-+->> ~expr ~@forms))

(defmacro let-par
  [bindings & forms]
  `(to-be-continued.macros/let-par ~bindings ~@forms))