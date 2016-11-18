(ns frontend.main
  (:require [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :refer [hatch-positions]]
            [frontend.loc :as loc]))

(defn run []
  (js/console.log "Jiihaaz!"))

(-> js/window .-onload (set! run))
