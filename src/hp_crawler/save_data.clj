(ns hp-crawler.save-data
  "Provides functions to store and retrieve the fetched data."
  (:require
    [clojure.string :as s]
;    [clj-time.core :as time]
;    [clj-time.coerce :as tc]
    [clj-time.format :as tf]
    [clj-time.local :as tl]
    [clj-time.core :as tc]
    [incanter.core :as ic]
    [clojure.java.io :as io]
    )
  )

(def ^{:doc "The format of the processing date of a data record."}
  rec-format
  (tf/formatters :date-hour-minute-second))

;;TODO: create a different namespace to handle date-time.
(defn str-date->timestamp
  "Converts a string date to timestamp."
  [str-date]
  (->> (tf/parse rec-format str-date)
       (tc/interval (tc/epoch))
       (tc/in-msecs)))

(defn date-time-map->file!
  "Appends a new record to the given filename, where a record is composed of
  date-time and a clojure map, separated by a semicolon."
  [filename data-map]
  (spit filename 
        (s/join ";" [(tl/format-local-time (tl/local-now) :date-hour-minute-second)
                     (str data-map "\n")])
        :append true))

(defn file->dataset
  "Reads the contents of a file and returns an incanter dataset with them. The save
  time is converted to timestamp."
  [filepath]
  (with-open [rdr (io/reader filepath)]
    (-> (map #(let [[date-str data] (s/split % #";")]
                (-> (read-string data)
                    (assoc :save-date (str-date->timestamp date-str))))
             (line-seq rdr))
        doall
        (ic/to-dataset))))
