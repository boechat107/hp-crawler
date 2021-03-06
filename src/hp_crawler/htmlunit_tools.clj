(ns hp-crawler.htmlunit-tools
  (:import
    [com.gargoylesoftware.htmlunit WebClient BrowserVersion]
    [com.gargoylesoftware.htmlunit.html HtmlPage DomNode]
    )
  )

(defn wait-scripts!
  "Stops the htmlunit main thread for an amount of time wt. Returns the same page
  object."
  [page wt]
  {:pre [(instance? HtmlPage page)]}
  (when (pos? wt)
    (locking page
      (println "Waiting scripts...")
      (.wait page wt)))
  page)

(defn browse-page
  "Returns a htmlunit page object for the given URL. A waiting time can be specified
  (zero is the default), allowing the htmlunit background threads to work a little
  more before the manipulation of the page. It's useful for webpages whose contents
  are loaded by javascripts."
  ([url] (browse-page url 0))
  ([url wt]
   {:pre [(string? url)]}
   ;; Suppress Htmlunit logs.
   (-> (java.util.logging.Logger/getLogger "com.gargoylesoftware")
       (.setLevel java.util.logging.Level/INFO))
   (-> BrowserVersion/FIREFOX_3_6
       (WebClient.)
       (.getPage url)
       (wait-scripts! wt))))

(defn get-nodes-by-xpath
  "Returns htmlunit nodes that represents html elements for the given xpath and 
  parent node."
  [node xpath]
  {:pre [(instance? DomNode node) (string? xpath)]}
  (.getByXPath node xpath))

(defn refresh-page!
  "Refreshes the page of the given object."
  [page]
  {:pre [(instance? HtmlPage page)]}
  (.refresh page))
