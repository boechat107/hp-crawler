(ns hp-crawler.htmlunit-tools
  (:import
    [com.gargoylesoftware.htmlunit WebClient BrowserVersion]
    )
  )

(defn browse-page
  "Returns a htmlunit page object for the given URL."
  [url]
  {:pre [(string? url)]}
  (-> BrowserVersion/FIREFOX_10
      (WebClient.)
      (.getPage url)))

(defn get-nodes-by-xpath
  "Returns htmlunit nodes that represents html elements for the given xpath and 
  parent node."
  [node xpath]
  (.getByXPath node xpath))
