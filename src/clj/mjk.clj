(ns mjk
  (:require [ring.util.response :as response]
            [ring.middleware.content-type :as content-type]))

(defn resource-handler [{uri :uri}]
  (if (= uri "/")
    (-> (response/resource-response "/index.html")
        (assoc-in [:headers "content-type"] "text/html"))
    (-> (response/resource-response uri)
        (or (response/not-found "Not found")))))

(def handler (-> resource-handler
                 (content-type/wrap-content-type)))
