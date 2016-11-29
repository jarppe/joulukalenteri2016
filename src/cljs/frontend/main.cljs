(ns frontend.main
  (:require [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :as hatch-pos]))

(def image-width 2048)
(def image-height 1448)
(def image-ratio (/ image-width image-height))
(def t-image-index 3)                                       ; http://images.huffingtonpost.com/2016-07-15-1468607338-43291-DonaldTrumpangry.jpg

(def debug (atom {:active :k
                  :offset {:k {:x 0 :y 0}
                           :d {:x 0 :y 0}
                           :r {:x 0 :y 0}}}))

(defn debug! []
  (swap! debug update :active {:k :d :d :r :r :k}))

(defn debug-move! [axis value]
  (swap! debug (fn [{:keys [active] :as debug}]
                 (if (= active :k)
                   debug
                   (update-in debug [:offset active axis] + value)))))

; Original code from http://increasinglyfunctional.com/2013/12/08/point-polygon-clojure/

(defn- crossing-number
  "Determine crossing number for given point and segment of a polygon.
   See http://geomalgorithms.com/a03-_inclusion.html"
  [[px py] [[x1 y1] [x2 y2]]]
  (if (or (and (<= y1 py) (> y2 py))
          (and (> y1 py) (<= y2 py)))
    (let [vt (/ (- py y1) (- y2 y1))]
      (if (< px (+ x1 (* vt (- x2 x1))))
        1 0))
    0))

(defn inside?
  "Is point inside the given polygon?"
  [point polygon]
  (odd? (reduce + (for [n (range (dec (count polygon)))]
                    (crossing-number point [(nth polygon n) (nth polygon (inc n))])))))

(def active-hatch-count
  24
  #_(let [now (js/Date.)]
      (cond
        (> (.getYear now) 116) 24
        (< (.getMonth now) 11) 0
        :else (.getDate now))))

(defn ->hatch [n polygon]
  {:n n
   :polygon polygon
   :can-open? (< n active-hatch-count)
   :open? true})

(def hatches (atom (->> hatch-pos/hatches
                        (map-indexed ->hatch)
                        (vec))))

(defn hatch-path [ctx polygon]
  (let [[[x y] & more] polygon]
    (.moveTo ctx x y)
    (doseq [[x y] more]
      (.lineTo ctx x y))))

(def MIN -10000000)
(def MAX +10000000)

(defn draw-hatch-number [ctx n polygon can-open?]
  (let [[minx miny maxx maxy] (reduce (fn [[minx miny maxx maxy] [x y]]
                                        [(Math/min minx x) (Math/min miny y)
                                         (Math/max maxx x) (Math/max maxy y)])
                                      [MAX MAX MIN MIN]
                                      polygon)
        x (+ minx (* (- maxx minx) 0.5))
        y (+ miny (* (- maxy miny) 0.5))]
    (js/console.log n x y)
    (aset ctx "fillStyle" (if can-open? "rgba(0,128,0,0.9)" "rgba(128,0,0,0.9)"))
    (aset ctx "font" "128px serif")
    (aset ctx "textAlign" "center")
    (aset ctx "textBaseline" "middle")
    (.fillText ctx (str (inc n)) x y)))

(defn draw-hatch-image [ctx n images]
  (if (= n t-image-index)
    (doto ctx
      (.save)
      (.translate 569 430)
      (.scale 0.6 0.6)
      (.rotate -0.3)
      (.drawImage (:t images) 0 0)                          ; 120 164  1.27
      (.restore))
    (.drawImage ctx (:r images) 0 0)))

(defn draw-hatches [ctx images]
  (doseq [{:keys [polygon open? hover? can-open? n]} @hatches]
    (when open?
      (doto ctx
        (.save)
        (.beginPath)
        (hatch-path polygon)
        (.clip)
        (draw-hatch-image n images)
        (.restore)))
    (when hover?
      (doto ctx
        (.save)
        (aset "fillStyle" (if can-open? "rgba(0,255,0,0.5)" "rgba(255,0,0,0.5)"))
        (.beginPath)
        (hatch-path polygon)
        (.fill)
        (draw-hatch-number n polygon can-open?)
        (.restore)))))

(defn repaint [canvas images]
  (js/console.log "repaint")
  (let [width (-> canvas .-offsetWidth)
        height (/ width image-ratio)
        scale (/ width image-width)
        ctx (.getContext canvas "2d")]
    (doto canvas
      (aset "width" width)
      (aset "height" height))
    (doto ctx
      (.save)
      (.scale scale scale))
    (let [{:keys [active offset]} @debug
          {:keys [x y]} (active offset)]
      (js/console.log "repaint" (name active) x y)
      (doto ctx
        (.save)
        (.translate x y)
        (.drawImage (active images) 0 0)
        (.restore)))
    (doto ctx
      (draw-hatches images)
      (.restore))))

(defn canvas-pos [canvas e]
  (let [width (-> canvas .-offsetWidth)
        scale (/ width image-width)
        ox (-> canvas .-offsetLeft)
        oy (-> canvas .-offsetTop)
        x (-> e .-pageX (- ox) (/ scale))
        y (-> e .-pageY (- oy) (/ scale))]
    [x y]))

(defn hovering [pos {:keys [polygon hover?] :as hatch}]
  (let [now-hover? (inside? pos polygon)]
    (if (not= now-hover? hover?)
      (assoc hatch :hover? now-hover?)
      hatch)))

(defn clicked [pos {:keys [polygon] :as hatch}]
  (if (inside? pos polygon)
    (update hatch :open? not)
    hatch))

(defn update-hatches [f]
  (let [new-hatches (mapv f @hatches)]
    (if (not= @hatches new-hatches)
      (reset! hatches new-hatches))))

(defn mouse-move [canvas e]
  (update-hatches (partial hovering (canvas-pos canvas e))))

(defn mouse-click [canvas e]
  (let [[x y] (canvas-pos canvas e)]
    (js/console.log (str "[" (.toFixed x 0) " " (.toFixed y 0) "]")))

  #_(update-hatches (partial clicked (canvas-pos canvas e))))

(defn get-element [element]
  (or (.getElementById js/document element)
      (-> (.getElementsByTagName js/document element)
          (aget 0))))

(defn init! []
  ; Remove "loading..." div:
  (let [loading (get-element "loading")]
    (-> loading .-parentNode (.removeChild loading)))
  ; Make header, canvas and footer visible:
  (doseq [e (map get-element ["header" "canvas" "footer"])]
    (aset e "style" ""))
  ; Make a repaint, register it to places and invoke it.
  (let [canvas (get-element "canvas")
        images (into {} (map (juxt keyword get-element) ["k" "r" "t" "d"]))
        repaint (partial repaint canvas images)]
    (.addEventListener canvas "mousemove" (partial mouse-move canvas))
    (.addEventListener canvas "click" (partial mouse-click canvas))
    (.addEventListener js/window "keydown" (fn [e]
                                             (condp = (.-key e)
                                               "d" (debug!)
                                               "ArrowLeft" (debug-move! :x -2)
                                               "ArrowRight" (debug-move! :x +2)
                                               "ArrowUp" (debug-move! :y -2)
                                               "ArrowDown" (debug-move! :y +2)
                                               nil)
                                             (.preventDefault e)
                                             (repaint)))
    (.addEventListener js/window "resize" repaint)
    (add-watch hatches :hatch-open-watch repaint)
    (repaint)))

(aset js/window "onload" init!)
