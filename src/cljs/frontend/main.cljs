(ns frontend.main
  (:require [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :as hatch-pos]
            [frontend.point-polygon :refer [inside?]]))

(def image-width 2048)
(def image-height 1448)
(def image-ratio (/ image-width image-height))

(def active-hatch-count
  (let [now (js/Date.)]
    (if (-> now .getFullYear (> 2016))
      (count hatch-pos/hatches)
      (-> now .getDate inc))))

(defn ->hatch [n hatch]
  (let [default (fn [default-value]
                  (fn [value]
                    (or value default-value)))]
    (-> hatch
        (assoc :can-open? (< n active-hatch-count)
               :id n)
        (update :polygon (fn [[start :as polygon]] (conj polygon start)))
        (update :translate (default [0 0]))
        (update :image (default :r))
        (update :scale (default [1 1]))
        (update :rotate (default 0))
        (update :label (default (str n))))))

(def hatches (atom (->> hatch-pos/hatches
                        (map ->hatch (range))
                        (vec))))
(def opened (local-storage (atom (zipmap (-> hatch-pos/hatches count range)
                                         (repeat false)))
                           :opened-2016))

(defn hatch-path [ctx polygon]
  (let [[[x y] & more] polygon]
    (.moveTo ctx x y)
    (doseq [[x y] more]
      (.lineTo ctx x y))))

(defn draw-hatch-number [ctx label polygon can-open? open?]
  (if-not open?
    (let [MIN -1000000
          MAX +1000000
          [minx miny maxx maxy] (reduce (fn [[minx miny maxx maxy] [x y]]
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
      (.fillText ctx label x y))))

(defn draw-hatch-image [ctx {image :image [trx try] :translate [sx sy] :scale r :rotate} images]
  (doto ctx
    (.save)
    (.translate trx try)
    (.scale sx sy)
    (.rotate r)
    (.drawImage (images image) 0 0)
    (.restore)))

(defn draw-hatches [ctx images]
  (doseq [{:keys [polygon id hover? can-open? label] :as hatch} @hatches]
    (let [open? (get @opened id)]
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
          (draw-hatch-number label polygon can-open? open?)
          (.restore))))))

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
      (.scale scale scale)
      (.drawImage (:k images) 0 0)
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

(defn update-hatches [f]
  (let [new-hatches (mapv f @hatches)]
    (if (not= @hatches new-hatches)
      (reset! hatches new-hatches))))

(defn mouse-move [canvas e]
  (update-hatches (partial hovering (canvas-pos canvas e))))

(defn mouse-click [canvas e]
  (let [pos (canvas-pos canvas e)
        hatch (some (fn [hatch]
                      (if (inside? pos (:polygon hatch))
                        hatch))
                    @hatches)]
    (if (and hatch (:can-open? hatch))
      (swap! opened update (:id hatch) not))))

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
    (.addEventListener js/window "resize" repaint)
    (add-watch hatches :hatch-open-watch repaint)
    (add-watch opened :hatch-open-watch repaint)
    (repaint)))

(aset js/window "onload" init!)
