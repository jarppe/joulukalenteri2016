(ns frontend.point-polygon)

; Original code from http://increasinglyfunctional.com/2013/12/08/point-polygon-clojure/

(defn crossing-number
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
