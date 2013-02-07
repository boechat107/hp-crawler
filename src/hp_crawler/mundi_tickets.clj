(ns hp-crawler.mundi-tickets
  (:require
    [hp-crawler.save-data :as save]
    [clojure.string :as s]
    [hp-crawler.utils :as ut]
    [clj-webdriver.core :as wc]
    [clj-webdriver.wait :as ww]
    [clj-webdriver.taxi :as wt]
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
  extracted from the given page object.
  XPATH strings are used to select the desired elements of the page."
  [page]
  (let [find-elem (fn [xp] (wc/find-elements page {:xpath xp}))
        xp1 "//div[@id='menu_busca']//form[@id='filters']//fieldset[@id='airlinefilter']//tr[@class='airlineCB']"
        ;; Parses the string result of xp1.
        xp1-parser (fn [t] (s/split t #" |\n"))
        xp1-elems (find-elem xp1)
        ;; Sometimes, I don't know the reason, the layout of the site is different.
        ;; So, I needed another xpath.
        xp2 "//section[@id='filter-by-airline']//div[@class='box1']//li[@class='ui-item']"
        ;; Parses the string result of xp2
        ;; ("Múltiplas\nR$ 1041" "Gol\nR$ 2254" "Azul\nR$ 1097" "TRIP\nR$ 1274" "Tam\nR$ 1961")
        xp2-parser (fn [t] (let [r (s/split t #" |\n")] [(first r) (peek r)]))
        xp2-elems (find-elem xp2)
        pt #(do (println (str %)) %)
        res-to-map (fn [elems parser] 
                     (->> elems
                          (reduce #(let [[comp price] (-> (wt/text page %2)
                                                          pt
                                                          parser)]
                                     (assoc %1 comp (Integer/parseInt price)))
                                  {})))
        found-elems? (fn [elems] (seq (filter #(wt/exists? page %) elems)))]
    (cond 
      (found-elems? xp1-elems) (res-to-map xp1-elems xp1-parser)
      ;(map #(wt/text page %) xp1-elems)
      (found-elems? xp2-elems) (res-to-map xp2-elems xp2-parser)
      :else nil)
    ))

(defn extract-data
  "Executes the scrap tasks and verify the results, reloading the website if the
  tasks fail."
  [page]
  (ut/try-times
    2
    (println "Trying to fetch data...")
    (let [data (get-minimum-prices page)]
      (if data
        data
        (do
          (println "Not fetched!")
          ;; Sleeps for a random time.
          (Thread/sleep (+ 1000 (rand-int 5000)))
          ;; If the data was not fetched, the page is refreshed.
          (wt/refresh page)
          (throw+ {:type ::min-price, :message "Fail to fetch prices"})))))
  )

(defn scrap
  "Prepares a seed URL and returns information from the target page. As the scraper
  shall run just once in a hour, there is no loop in this function.
  Ex.: (scrap 'fln' 'bhz' '01-02-2013' '03-02-2013')"
  ; TODO: log activities using timbre
  [filepath dep-airp ret-airp dep-date ret-date]
  (let [url (make-seed dep-airp ret-airp dep-date ret-date)
        wait-time 10000 ; milliseconds
        browser (wc/new-driver {:browser :firefox})]
    ;; Sets the amount of time that must be wait when the desired elements of the
    ;; page are not found. Useful for AJAX pages.
    (ww/implicit-wait browser wait-time)
    (wt/to browser url)
    (try+
      (let [res (-> (extract-data browser)
                    (assoc :dep-airp dep-airp
                           :ret-airp ret-airp 
                           :dep-date dep-date
                           :ret-date ret-date))]
        (save/date-time-map->file! filepath res)
        (wt/quit browser)
        res)
      (catch [:type ::min-price] e
        (println (:message e))
        (wt/quit browser)))))


