# To Be Continued

To Be Continued is a library that simplifies asynchronous programming. It allows synchronous and asynchronous code to be blended together, handling the generation of the necessary callback functions automatically.

## Installation

Add the following dependency to your project's project.clj file:

```clojure
[to-be-continued "0.1.0-SNAPSHOT"]
```

Then add the appropriate require statements to the namespaces in which you want to use To Be Continued. Even if you are not directly calling the functions defined in the `to-be-continued.fns` namespace, it still must be required for use by the macros.

### Clojure
```clojure
(:require [to-be-continued.macros :as tbc]
          [to-be-continued.fns :as tbc-fns])
```

For Clojure only, the `to-be-continued.core` namespace is provided as a convenience. It includes both the macros and the functions.

### ClojureScript

```clojure
(:require-macros [to-be-continued.macros :as tbc])
(:require [to-be-continued.fns :as tbc-fns])
```

## Async-Aware Threading Macros

The `-+->` and `-+->>` macros are asynchronous-aware equivalents of Clojure's `->` and `->>` macros, respectively. When any of the intermediate forms in the threading expression ends in `...`, it signals that the form is invoking asynchronous code and requires a callback. To Be Continued will replace the `...` with an automatically generated callback function that will continue with the threading expression where the asynchronous call left off.

For example:

```clojure
(defn fetch-hobbies
  "Given a person map, fetches the person's hobbies and passes them
   to the specified continuation."
  [person callback]
  (tbc/-+-> person
            :login
            (fetch-profile ...)
            :hobbies
            callback))
```

In this case, `fetch-profile` is a hypothetical function that asynchronously fetches the profile of a person. It takes two arguments: (1) the login name of the person and (2) a callback to invoke with the profile is available. The `-+->` macro generates a callback function representing the continuation of the thread (i.e., resuming with `:hobbies`).

Note that the return values of `-+->` and `-+->>` are undefined. It is expected that the final form in the thread will be the invocation of a callback to hand over control to whatever code is expecting the result.

## Parallel Binding of Asynchronous Results
The `let-par` macro is the asynchronous equivalent of Clojure's `let` macro. It allows the results of multiple asynchronous functions, executed in parallel, to be bound to variables that can be referenced the body expression.

For example:

```clojure
(defn make-breakfast
  [callback]
  (tbc/let-par [ham  (get-ham ...)
                eggs (get-eggs ...)]
    (tbc/-+-> (cook ham eggs)
              callback)))
```

The `get-ham` and `get-eggs` asynchronous functions will be executed in parallel. When both have completed, the body of the `let-par` expression will be evaluated with `ham` and `eggs` bound to their results.

Note that the use of the `-+->` macro above is not strictly necessary because the `cook` function is synchronous. It use is encouraged, however, because--once error handling is implemented; see the Roadmap section below--it will properly handle any errors that occur.

Like `-+->` and `-+->>`, the value of a `let-par` expression is undefined. It is expected to invoked a callback with its result.

## Parallel Mapping an Asynchronous Function over a Collection
The `map-par` function is the asynchronous equivalent of Clojure's `map` function. It takes an asynchronous function of two arguments--an input value and a callback--and invokes it for each member of a collection. When all of the results are available, they are passed as a vector to the specified callback.

For example:

```clojure
(tbc-fns/map-par fetch-profile some-callback)
```

Like any other asynchronous function that takes a callback as its last argument, `map-par` expressions may be included in `-+->` and `-+->>` threading expressions or used as `let-par` binding forms.

## Example Application
Please see [tbc-node-example](https://github.com/gregspurrier/tbc-node-demo) for an example ClojureScript + Node.js application that uses To Be Continued to asynchronously query the GitHub API.

## Roadmap
- Error handling
- Additional means of parallel execution 

## License

Copyright (c) 2012 Greg Spurrier

Distributed under the MIT license. See LICENSE.txt for the details.
