(ns frontend.main
  (:require [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :refer [hatch-positions]]
            [frontend.loc :as loc]))

(def base-img "img/k.jpeg")
(def revealed-img "img/r.jpeg")
(defn px [v] (str v "px"))

(def active-hatches
  24
  #_(let [now (js/Date.)]
      (cond
        (> (.getYear now) 116) 24
        (< (.getMonth now) 11) 0
        :else (.getDate now))))

(defn make-hatches []
  (map (fn [{:keys [x y x2 y2 r]} n can-open?]
         {:n n
          :x (- x 4)
          :y (- y 6)
          :w (- x2 x 4)
          :h (- y2 y 6)
          :r r
          :can-open? can-open?})
       hatch-positions
       [1 2]
       #_(range 1 25)
       (concat (repeat active-hatches true)
               (repeat false))))

(defn hatch-component [{:keys [n x y w h r can-open?]} opened? scale]
  (let [mouse-click (fn [_]
                      (if can-open?
                        (swap! opened? not)))
        touch-start (fn [_]
                      (js/console.log "touch start"))
        touch-end (fn [_]
                    (js/console.log "touch end"))
        touch-cancel (fn [_]
                       (js/console.log "touch cancel"))]
    (js/console.log "scale:" scale)
    [:div.hatch {:class (str (if can-open? "allowed " "forbidden ")
                             (if true #_@opened? "opened " "closed "))
                 :style {:left (px (* scale x))
                         :top (px (* scale y))
                         :width (px (* scale w))
                         :height (px (* scale h))
                         :line-height (px (* scale h))
                         :transform (str "rotate(" (or r 0) "deg)")}
                 :on-click mouse-click
                 :on-touch-start touch-start
                 :on-touch-end touch-end
                 :on-touch-cancel touch-cancel}
     (if (and can-open? #_@opened?)
       [:img {:style {:left "0px" #_ (px (- (/ x scale)))
                      :top "0px" #_ (px (- (/ y scale)))
                      :transform (str "scale(" scale ")")}
              :src revealed-img}])
     [:div.label
      (str n)]]))

(defn flag [flag-lang lang]
  [:a {:on-click (fn [_] (reset! lang flag-lang))}
   [:img {:src (str "/img/" flag-lang ".png")
          :class (if (= flag-lang @lang) "active")}]])

(defn image [{:keys [src element]}]
  (r/create-class
    {:component-did-mount
     (fn [this]
       (reset! element (r/dom-node this)))
     :render
     (fn [_]
       [:img#main-image {:src src}])}))

(defn main-view []
  (let [opened (local-storage (r/atom {}) :opened-2016)
        lang (r/atom (if (= js/window.lang "fi") "fi" "en"))
        image-element (r/atom nil)
        window-width (r/atom nil)
        image-width (r/track (fn []
                               @window-width
                               (some->> image-element
                                        deref
                                        .-offsetWidth)))]
    (js/window.addEventListener "resize" (fn []
                                           (reset! window-width (.-innerWidth js/window))))
    (fn []
      (let [terms (get loc/terms @lang)]
        [:div#app
         [:div.lang
          [flag "fi" lang]
          [flag "en" lang]]
         [:header
          [:h1 (terms :title)]
          [:h2 (terms :help)]]
         [:article {:style {:display "flex"
                            :flex-direction "column"
                            :align-items "stretch"
                            :align-content "stretch"}}
          (let [scale (/ @image-width 2048.0)]
            [:div#image-wrapper
             (for [{n :n :as hatch} (make-hatches)]
               ^{:key n} [hatch-component hatch (r/cursor opened [n]) scale])])
          [image {:src base-img :element image-element}]]
         [:footer
          [:p (terms :art-copy)]
          [:p
           [:a {:href "https://github.com/jarppe/joulukalenteri2016" :target "_blank"} (terms :code)]
           " "
           (terms :code-copy)]]]))))

(defn init! []
  (js/console.log "here we go!")
  (r/render [main-view] (js/document.getElementById "app")))

(init!)
