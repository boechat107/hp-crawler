(ns hp-crawler.core
  (:use
    [clojure.tools.cli :only [cli]])
  (:require
    [hp-crawler.mundi-tickets :as ft])
  (:gen-class))

(defn -main
  [& args]
  (let [[opts _ banner]
        (cli args 
             ["-h" "--help" "Show help" :flag true :default false]
             ["-f" "--file" "File path to store the results"]
             ["-a1" "--departure-airport" "Departure airport (3 letters)"]
             ["-a2" "--destination-airport" "Destination airport (3 letters)"]
             ["-d1" "--departure-date" "Like dd-mm-yyyy or dd/mm/yyyy"]
             ["-d2" "--returning-date" "Like dd-mm-yyyy or dd/mm/yyyy"])
        ;; Arguments of the flight ticket scraper.
        ft-args (map #(% opts) [:file :departure-airport :destination-airport 
                                :departure-date :returning-date])]
    (cond
      ;; Prints the help message.
      (:help opts) (println banner)
      ;; Executes the flight tickets scraper.
      (every? identity ft-args) (apply ft/scrap ft-args))))
