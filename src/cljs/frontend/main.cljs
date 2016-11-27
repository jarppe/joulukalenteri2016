(ns frontend.main
  (:require [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :refer [hatch-positions]]
            [frontend.loc :as loc]))

(def revealed-img "img/r.jpeg")
(defn px [v] (str v "px"))

(def active-hatches
  12
  #_(let [now (js/Date.)]
      (cond
        (> (.getYear now) 116) 24
        (< (.getMonth now) 11) 0
        :else (.getDate now))))

(defn make-hatches []
  (map (fn [{:keys [x y x2 y2]} n can-open?]
         {:n n
          :x (- x 4)
          :y (- y 6)
          :w (- x2 x 4)
          :h (- y2 y 6)
          :can-open? can-open?})
       hatch-positions
       (range 1 25)
       (concat (repeat active-hatches true)
               (repeat false))))

(defn make-tooltip-message [terms n]
  (let [days-until (- n active-hatches)]
    (or (get-in terms [:until days-until])
        ((terms :until-more) days-until))))

(defn active-hatch-component [{:keys [n x y w h]} opened?]
  [:div.hatch.allowed {:class (if @opened? "opened" "closed")
                       :style {:left (px x)
                               :top (px y)
                               :width (px w)
                               :height (px h)
                               :line-height (px h)}
                       :on-click (fn [_] (swap! opened? not) nil)}
   (if @opened?
     [:img {:style {:position "relative"
                    :left (px (* x -1))
                    :top (px (* y -1))}
            :src revealed-img}])])

(defn inactive-hatch-component [lang {:keys [n x y w h]}]
  (let [tooltip? (r/atom false)
        tooltip! (fn [v]
                   (js/console.log "tooltip!" v)
                   (reset! tooltip? v))
        tooltip-task (atom nil)
        mouse-enter (fn [_]
                      (js/clearTimeout @tooltip-task)
                      (reset! tooltip-task (js/setTimeout tooltip! 500 true))
                      nil)
        mouse-leave (fn [_]
                      (js/clearTimeout @tooltip-task)
                      (reset! tooltip-task (js/setTimeout tooltip! 100 false))
                      nil)
        touch-start (fn [_]
                      (js/console.log "touch start"))
        touch-end (fn [_]
                    (js/console.log "touch end"))
        touch-cancel (fn [_]
                       (js/console.log "touch cancel"))
        touch-move (fn [_]
                     (js/console.log "touch move"))]
    (fn []
      [:div
       [:div.hatch {:class "closed forbidden"
                    :style {:left (px x)
                            :top (px y)
                            :width (px w)
                            :height (px h)
                            :line-height (px h)}
                    :on-mouse-enter mouse-enter
                    :on-mouse-leave mouse-leave
                    :on-touch-start touch-start
                    :on-touch-end touch-end
                    :on-touch-cancel touch-cancel}]
       (if @tooltip?
         [:div.tooltip {:style {:left (px x)
                                :top (px (+ y h 10))}}
          [:p (let [tooltip-message (make-tooltip-message (loc/terms @lang) n)]
                (js/console.log "tooltip:" tooltip-message)
                tooltip-message)]])])))

(defn hatch-component [lang {:keys [can-open?] :as hatch} opened?]
  (if can-open?
    [active-hatch-component hatch opened?]
    [inactive-hatch-component lang hatch]))

(defn flag [flag-lang lang]
  [:a {:on-click (fn [_] (reset! lang flag-lang))}
   [:img {:src (str "/img/" flag-lang ".png")
          :class (if (= flag-lang @lang) "active")}]])

(defn main-view []
  (let [opened (local-storage (r/atom {}) :opened-2016)
        lang (r/atom (or js/window.lang "en"))]
    (fn []
      (let [terms (get loc/terms @lang)]
        [:div#app
         [:div.lang
          [flag "fi" lang]
          [flag "en" lang]]
         [:header
          [:h1 (terms :title)]
          [:h2 (terms :help)]]
         [:article
          [:div#image-wrapper
           (for [{n :n :as hatch} (make-hatches)]
             ^{:key n} [hatch-component lang hatch (r/cursor opened [n])])]
          [:img#main-image {:src "img/k.jpeg"}]]
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
