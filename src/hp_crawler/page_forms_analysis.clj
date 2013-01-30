(ns hp-crawler.page-forms-analysis
    (:require
      [clojure.java.io :as io]
      [net.cgrand.enlive-html :as html])
    )

(def ^{:doc "The content of the web page whose analysis is desired."}
     web-file
     "resources/mundi_passagens.html")

(defn get-file-content 
  ([] (get-file-content web-file))
  ([filepath]
   (io/reader filepath)))

(defn list-forms
  []
  (->> (-> (get-file-content) (html/html-resource) (html/select [:form]))
       (map :attrs)))

(defn list-inputs!
  ([] (list-inputs! nil))
  ([form-id]
   (let [page (html/html-resource (get-file-content))]
     (if form-id
       (html/select page [[:form (html/attr= :id form-id)] :input])
       (doseq [form (html/select page [:form])]
         (println (str "==== "(:id form) " ====")) 
         (println (html/select (:content form) [:input]))
         )
       )
     )
   ))
