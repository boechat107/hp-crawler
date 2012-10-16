(ns hp-crawler.downloader
    (:require
      [clj-http.client :as http]
      [clj-dns.core :as dns]
      )
    (:import
      [org.xbill.DNS Type]
      )
    )

; ### TODO: 
; * try curl
; * cache dns lookups


(defn download-page
  "todo: macro with-connection-pool, more than one thread"
  [url]
  (http/get url))


(defn get-domain-ip
  [url]
  (-> (dns/dns-lookup url)
      (:answers)
      (first)
      (.getAddress)
      (.getHostAddress)))

;www.qnumero.com.br/?nome=empresa+viacao+nasser+ltda&cidade=mococa+sp&btnG=Buscar

