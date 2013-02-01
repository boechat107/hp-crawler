(ns hp-crawler.mundi-tickets
  (:require
    [clojure.string :as s]
    [hp-crawler.utils :as ut]
    [hp-crawler.htmlunit-tools :as hu]
    )
  )

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
  (->> "//div[@id='menu_busca']//form[@id='filters']//fieldset[@id='airlinefilter']//tr[@class='airlineCB']"
       (hu/get-nodes-by-xpath page)
       seq
       (reduce #(let [[comp price] (-> (.getTextContent %2) (s/split #" "))]
                  (assoc %1 comp (Integer/parseInt price)))
               {})))

(defn scrap
  "Prepares a seed URL and returns information from the target page."
  []
  (let [url (make-seed "fln" "bhz" "01-02-2013" "03-02-2013")
        browser (hu/browse-page url 30000)]
    (println "Waiting the javascripts to run...")
    (get-minimum-prices browser)))
