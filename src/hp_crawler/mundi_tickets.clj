(ns hp-crawler.mundi-tickets
  (:require
    [clojure.string :as s]
    [hp-crawler.utils :as ut]
    )
  )

(def ^{:doc "URL with the fields for a query. The default values are roundtrip and
            just one adult passenger.
            It's an example of a query and could be outdated."}
  pre-seeds
  "http://www.mundi.com.br/flightmetasearch?airport1=FLN&airport2=BHZ&triptype=1&airport1_text=Florian%C3%B3polis%2C+SC+-+Florianopolis+%28FLN%29&airport2_text=Belo+Horizonte%2C+MG+-+Todos+aeroportos+%28BHZ%29&date1=01%2F02%2F2013&date2=03%2F02%2F2013&numadults=1&numchildren=0&numbabies=0"
  )

(defn replace-dates
  "Returns a valid URL replacing the original the departure and return dates by new
  ones."
  [url-seed d1 m1 y1 d2 m2 y2]
  ;; TODO: option to omit the year, taking it from the system time.
  (let [d-m #(ut/fill-zeros-left (str %) 2)] ; converts a number to dd or mm.
    (-> url-seed
        (s/replace #"date1=[F\d%]*&" " hello ")
        ))
  )
