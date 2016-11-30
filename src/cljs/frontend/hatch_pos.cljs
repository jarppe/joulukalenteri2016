(ns frontend.hatch-pos)

(defn rect [x1 y1 x2 y2]
  [[x1 y1] [x2 y1] [x2 y2] [x1 y2]])

(def hatches [{:label "30.11"
               :polygon (rect 516 1152 720 1249)
               :translate [0 22]}
              {:polygon (rect 496 871 594 1009)
               :translate [-5 20]}
              {:polygon (rect 30 827 85 884)
               :translate [0 15]}
              {:polygon (rect 1510 425 1539 456)
               :translate [-8 10]}
              {:polygon (rect 376 1000 466 1147)
               :translate [-2 22]}
              {:polygon (rect 132 161 206 321)
               :translate [-8 20]}
              {:polygon [[718 205]
                         [873 205]
                         [873 375]
                         [718 375]]
               :translate [-8 20]}
              {:polygon (rect 834 984 1098 1259)
               :translate [0 15]}
              {:polygon (rect 1567 627 1629 745)
               :translate [0 10]}
              {:polygon (rect 1253 200 1330 310)
               :translate [-8 14]}
              {:polygon (rect 1370 86 1496 440)
               :translate [0 14]}
              {:polygon [[1216 700]
                         [1430 700]
                         [1430 940]
                         [1367 1010]
                         [1216 1010]]
               :translate [0 8]}
              {:polygon (rect 1008 664 1153 847)
               :translate [-3 13]}
              {:polygon (rect 190 1024 360 1091)
               :translate [-2 22]}
              {:polygon [[695 910]
                         [743 810]
                         [797 810]
                         [797 1131]
                         [695 1131]]
               :translate [-5 20]}
              {:polygon (rect 1698 513 1843 699)
               :translate [-5 5]}
              {:polygon (rect 1562 1059 1693 1251)
               :translate [0 17]}
              {:polygon [[222 252]
                         [367 165]
                         [395 558]
                         [267 646]]
               :translate [0 30]}
              {:polygon [[733 456]
                         [755 397]
                         [861 397]
                         [861 525]
                         [743 525]]
               :translate [-7 18]}
              {:polygon [[569 430]
                         [636 409]
                         [665 507]
                         [598 527]]
               :translate [569 430]
               :image :t
               :scale [0.6 0.6]
               :rotate -0.3}
              {:polygon [[963 150]
                         [1212 150]
                         [1212 310]
                         [965 310]]
               :translate [0 10]}
              {:polygon (rect 624 684 780 780)
               :translate [0 10]}
              {:polygon (rect 472 136 636 365)
               :translate [-8 22]}
              {:polygon (rect 337 610 474 797)
               :translate [0 19]}
              {:polygon [[1604 1019]
                         [1587 997]
                         [1586 861]
                         [1927 858]
                         [1915 1234]
                         [1730 1224]
                         [1732 1159]
                         [1678 1051]]
               :translate [0 12]}])
