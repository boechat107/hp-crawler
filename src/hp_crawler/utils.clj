(ns hp-crawler.utils
  (:use 
    [slingshot.slingshot :only [throw+ try+]]
    )
  )

(defn fill-zeros-left
  "Returns a new string with the given size, filling with zeros at the beginning if
  its original size is lesser than the desired."
  [s size]
  (let [nz (- size (count s))]
    (if (pos? nz)
      (str (repeat nz "0") s)
      s)))

(defn try-times*
  [times thunk]
  {:pre [(not (neg? times))]}
  (loop [n times]
    (println "Number of attempts " n)
    (if-let [res (try+
                   [(thunk)]
                   (catch Object e 
                     (when (zero? n)
                       (throw+ e))))]
      (first res)
      (recur (dec n)))))

(defmacro try-times 
  "Executes thunk at most 'times' times. If an exception is thrown, will retry.
  Reference:
  http://stackoverflow.com/questions/1879885/clojure-how-to-to-recur-upon-exception"
  [times & body]
  `(try-times* ~times (fn [] ~@body)))
