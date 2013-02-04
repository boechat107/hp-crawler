(ns hp-crawler.mundi-tickets
  (:require
    [hp-crawler.save-data :as save]
    [clojure.string :as s]
    [hp-crawler.utils :as ut]
    [hp-crawler.htmlunit-tools :as hu])
  (:use 
    [slingshot.slingshot :only [throw+ try+]]))

(def ^{:doc "URL with the fields for a query. The default values are roundtrip and
            just one adult passenger.
            It's an example of a query and could be outdated."}
  ex-full-seeds
  "http://www.mundi.com.br/flightmetasearch?airport1=FLN&airport2=BHZ&triptype=1&airport1_text=Florian%C3%B3polis%2C+SC+-+Florianopolis+%28FLN%29&airport2_text=Belo+Horizonte%2C+MG+-+Todos+aeroportos+%28BHZ%29&date1=01%2F02%2F2013&date2=03%2F02%2F2013&numadults=1&numchildren=0&numbabies=0"
  )

(def ^{:doc "Simpler URL with blank fields (formatted to easily use the format
            function) for to query airport tickets. Only the airport nicknames are
            used.The default values are roundtrip and just one adult passenger."}
  ex-simp-seeds
  "http://www.mundi.com.br/flightmetasearch?airport1=FLN&airport2=BHZ&triptype=1&date1=01%2F02%2F2013&date2=03%2F02%2F2013&numadults=1&numchildren=0&numbabies=0"
  )

(def ^{:doc "The name of the file where the extracted information are stored."}
  file-storage
  "data/mundi_tickets.txt")

(defn make-seed
  "Returns a query URL for flight tickets at www.mundi.com.br.
  The query is composed of the departure airport, the destination airport, the
  departure date and the returning date. Airport names are strings of their
  nicknames, the dates are strings like dd-mm-yyyy or dd/mm/yyyy."
  [dep-airp dest-airp dep-date ret-date]
  (let [date-prep (fn [date] (->> (s/split date #"-|/")
                                  (s/join "%2F")))]
    (str "http://www.mundi.com.br/flightmetasearch?"
         "airport1=" (s/upper-case dep-airp) "&"
         "airport2=" (s/upper-case dest-airp) "&"
         "date1=" (date-prep dep-date) "&"
         "date2=" (date-prep ret-date) "&"
         "triptype=1&numadults=1&numchildren=0&numbabies=0")))

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

; ============== Parsing results, flight tickets ==============================

(defn get-minimum-prices
  "Returns a map with the minimum prices of each flight company. The prices are
  extracted from the given htmlunit page object."
  [page]
  (let [res (->> "//div[@id='menu_busca']//form[@id='filters']//fieldset[@id='airlinefilter']//tr[@class='airlineCB']"
                 (hu/get-nodes-by-xpath page)
                 seq
                 ;; Parses the company name and its corresponding ticket price.
                 ;; Like: 'Tam 700'
                 (reduce #(let [[comp price] (-> (.getTextContent %2) (s/split #" "))]
                            (assoc %1 comp (Integer/parseInt price)))
                         {}))]
    (if (empty? res)
      (throw+ {:type :min-price :message "Fail to fetch minimum prices information."})
      res)))

(defn scrap
  "Prepares a seed URL and returns information from the target page. As the scraper
  shall run just once in a hour, there is no loop in this function.
  Ex.: (scrap 'fln' 'bhz' '01-02-2013' '03-02-2013')"
  ; TODO: log activities 
  [dep-airp ret-airp dep-date ret-date]
  (let [url (make-seed dep-airp ret-airp dep-date ret-date)
        browser (hu/browse-page url 40000)]
    (ut/try-times
      2
      (try+
        ;; Try to fetch and parse the data.
        (println "Trying to parse...")
        (save/date-time-map->file! 
          file-storage 
          (-> (get-minimum-prices browser) 
              (assoc :dep-airp dep-airp
                     :ret-airp ret-airp 
                     :dep-date dep-date
                     :ret-date ret-date)))
        (println "Page parsed!")
        true ; to stop the function try-times
        (catch [:type :min-price] e
          (println e)
          ;; Sleeps for a random time.
          (Thread/sleep (+ 1000 (rand-int 5000)))
          ;; If the data was not fetched, the page is refreshed.
          (println "Refreshing the page...")
          (hu/refresh-page! browser)
          (hu/wait-scripts! browser 40000)
          nil)))
    ))


