(ns hp-crawler.utils)

(defn fill-zeros-left
  "Returns a new string with the given size, filling with zeros at the beginning if
  its original size is lesser than the desired."
  [s size]
  (let [nz (- size (count s))]
    (if (pos? nz)
      (str (repeat nz "0") s)
      s)))
