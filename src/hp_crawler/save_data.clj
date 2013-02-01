(ns hp-crawler.save-data
  "Provides functions to store and retrieve the fetched data."
  (:require
    [clojure.string :as s]
;    [clj-time.core :as time]
;    [clj-time.coerce :as tc]
    [clj-time.format :as tf]
    [clj-time.local :as tl]
    )
  )

(def ^{:doc "The format of the processing date of a data record."}
  rec-format
  (tf/formatters :date-hour-minute-second))

(defn date-time-map->file!
  "Appends a new record to the given filename, where a record is composed of
  date-time and a clojure map, separated by a semicolon."
  [filename data-map]
  (spit filename 
        (s/join ";" [(tl/format-local-time (tl/local-now) :date-hour-minute-second)
                     (str data-map "\n")])
        :append true))
