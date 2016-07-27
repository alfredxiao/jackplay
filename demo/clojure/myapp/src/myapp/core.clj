(ns myapp.core
  (:require [myapp.fun-things :as fun])
  (:gen-class))

(defn -main
  [greetee & rest]
  (dotimes [n 1000]
    (println (fun/greet greetee))
    (Thread/sleep 1200)))