(ns frontend.main
  (:require [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :as hatch-pos]))

(def image-width 2048)
(def image-height 1448)
(def image-ratio (/ image-width image-height))
(def t-image-index 3)                                       ; http://images.huffingtonpost.com/2016-07-15-1468607338-43291-DonaldTrumpangry.jpg

#_FIXME
(def debug (atom {:active :k
                  :offset {:k {:x 0 :y 0}
                           :d {:x 0 :y 0}
                           :r {:x 0 :y 0}}}))

#_FIXME
(defn debug! []
  (swap! debug update :active {:k :d :d :r :r :k}))

#_FIXME
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
  (let [now (js/Date.)
        year (-> now .getFullYear)
        month (-> now .getMonth inc)
        date (-> now .getDate)]
    (if (> year 2016)
      26
      (+ date 2))))

(defn default [default-value]
  (fn [value]
    (or value default-value)))

(defn ->hatch [{n :n :as hatch}]
  (-> hatch
      (assoc :can-open? (< n active-hatch-count)
             :open? #_FIXME true)
      (update :polygon (fn [[start :as polygon]] (conj polygon start)))
      (update :translate (default [0 0]))
      (update :image (default :r))
      (update :scale (default [1 1]))
      (update :rotate (default 0))
      (update :label (default (-> n inc str)))))

(def hatches (atom (->> hatch-pos/hatches
                        (sort-by :n)
                        (map ->hatch)
                        (vec))))

(defn hatch-path [ctx polygon]
  (let [[[x y] & more] polygon]
    (.moveTo ctx x y)
    (doseq [[x y] more]
      (.lineTo ctx x y))))

(def MIN -10000000)
(def MAX +10000000)

(defn draw-hatch-number [ctx label polygon can-open?]
  (let [[minx miny maxx maxy] (reduce (fn [[minx miny maxx maxy] [x y]]
                                        [(Math/min minx x) (Math/min miny y)
                                         (Math/max maxx x) (Math/max maxy y)])
                                      [MAX MAX MIN MIN]
                                      polygon)
        x (+ minx (* (- maxx minx) 0.5))
        y (+ miny (* (- maxy miny) 0.5))]
    (aset ctx "fillStyle" (if can-open? "rgba(0,128,0,0.9)" "rgba(128,0,0,0.9)"))
    (aset ctx "font" "110px serif")
    (aset ctx "textAlign" "center")
    (aset ctx "textBaseline" "middle")
    (.fillText ctx label x y)))

(defn draw-hatch-image [ctx {image :image [trx try] :translate [sx sy] :scale r :rotate} images]
  (doto ctx
    (.save)
    (.translate trx try)
    (.scale sx sy)
    (.rotate r)
    (.drawImage (images image) 0 0)
    (.restore)))

(defn draw-hatches [ctx images]
  (doseq [{:keys [polygon open? hover? can-open? label] :as hatch} @hatches]
    (when open?
      (doto ctx
        (.save)
        (.beginPath)
        (hatch-path polygon)
        (.clip)
        (draw-hatch-image hatch images)
        (.restore)))
    (when hover?
      (doto ctx
        (.save)
        (aset "fillStyle" (if can-open? "rgba(0,255,0,0.3)" "rgba(255,0,0,0.3)"))
        (.beginPath)
        (hatch-path polygon)
        (.fill)
        (draw-hatch-number label polygon can-open?)
        (.restore)))))

(defn repaint [canvas images]
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
    #_FIXME
    (let [{:keys [active offset]} @debug
          {:keys [x y]} (active offset)]
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
  (let [[x y :as pos] (canvas-pos canvas e)]
    #_FIXME
    (if (.-shiftKey e)
      (js/console.log (str "[" (.toFixed x 0) " " (.toFixed y 0) "]"))
      (update-hatches (partial clicked pos)))))

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
    #_FIXME
    (.addEventListener js/window "keydown" (fn [e]
                                             (condp = (.-key e)
                                               "d" (debug!)
                                               "ArrowLeft" (debug-move! :x -2)
                                               "ArrowRight" (debug-move! :x +2)
                                               "ArrowUp" (debug-move! :y -2)
                                               "ArrowDown" (debug-move! :y +2)
                                               nil)
                                             #_(.preventDefault e)
                                             (repaint)))
    (.addEventListener js/window "resize" repaint)
    (add-watch hatches :hatch-open-watch repaint)
    (repaint)))

(aset js/window "onload" init!)
