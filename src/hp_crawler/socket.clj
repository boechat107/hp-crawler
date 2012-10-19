(ns hp-crawler.socket
  (:import
   [java.net ServerSocket Socket SocketException]
   [java.io PrintWriter InputStreamReader BufferedReader]))

;; ## Server

(defn accept-connection
  [server-socket]
  (try
    (.accept server-socket)
    (catch SocketException e
      (println (format "Socket exception: %s" e)))))

(def exit-promise (promise))

(defn start-server!
  "Creates a socket server that accepts only one client at the same time."
  [port]
  (let [exit? (atom nil)]
    (future (if @exit-promise (reset! exit? true)))
    (with-open [s-socket (new ServerSocket port)
                c-socket (accept-connection s-socket)
                in<-client (new BufferedReader
                                (new InputStreamReader (.getInputStream c-socket)))
                out->client (new PrintWriter (.getOutputStream c-socket) true)]
      (loop [bool-exit @exit?]
        (if bool-exit
          (println "Server Stopped!")
          (do
            (println "Message received")
            (->> (.readLine in<-client)
                                        ;(println "Do something with:")
                 (.println out->client))
            (recur @exit?)))))))


(defn stop-server!
  []
  (deliver exit-promise true))

;; ## Client
(def in->client (atom nil))
(def out->server (atom nil))
(def client-socket (atom nil))

(defn start-client!
  [address port]
  (do
    (reset! client-socket (Socket. address port))
    (reset! out->server (new PrintWriter (.getOutputStream @client-socket) true))
    (reset! in->client (new BufferedReader
                             (new InputStreamReader (.getInputStream @client-socket))))))


(defn send-message!
  [m]
  (do
    (.println @out->server m)
    (println (.readLine @in->client))))


(defn stop-client!
  []
  (do
    (.close @in->client)
    (.close @out->server)
    (.close @client-socket)))



