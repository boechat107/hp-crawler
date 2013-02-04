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
  ;;TODO: stops even when thunk returns nil
  {:pre [(not (neg? times))]}
  (loop [n times]
    (println "Number of attempts " n)
    (if-let [res (try+
                   (thunk)
                   (catch Object e 
                     (when (zero? n)
                       (throw+ e))))]
      res
      (recur (dec n)))))

(defmacro try-times 
  "Executes thunk. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain.
  Reference:
  http://stackoverflow.com/questions/1879885/clojure-how-to-to-recur-upon-exception"
  [times & body]
  `(try-times* ~times (fn [] ~@body)))
