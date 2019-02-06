(ns cards
  (:require [clojure.spec.alpha :as s]))

(def cards
  [{:suite :diamonds
    :value :king}
   {:suite :clubs
    :value 4}])

(def cards-sorted
  (sort-by :suite cards))

(def deck
  (for [suite #{:diamonds :clubs :hearts :spades}
        value (range 13)
        :let [value (inc value)]]
    {:suite suite
     :value (case value
              1 :ace
              11 :jack
              12 :queen
              13 :king
              value)}))

(every? #(s/valid? ::card %) deck)

(def shuffled-deck
  (shuffle deck))

(defn draw-hand [deck]
  (let [hand (take 5 deck)
        deck (drop 5 deck)]
    {:hand hand
     :deck deck}))

(-> 4
    (inc ,)
    (dec ,)
    (* , 2))

(-> 4
    (inc ,)
    (dec ,)
    (*  2 ,))

(dec (inc 4))

(def hand (take 5 deck))

(def game {:players []
           :deck deck})

(-> game
    (assoc :deck deck)
    (update :players conj hand))

(defn start-game [deck player-count]
  (reduce (fn [{game-deck :deck
                :as game} n]
            (let [{:keys [deck hand]} (draw-hand game-deck)]
              (-> game
                  (assoc :deck deck)
                  (update :players conj hand))))
          {:players []
           :deck deck}
          (range player-count)))

(start-game shuffled-deck 3)

(defn game [deck]
  (let [after-p1 (draw-hand deck)
        after-p2 (draw-hand (:deck after-p1))]
    {:player1 (:hand after-p1)
     :player2 (:hand after-p2)
     :deck    (:deck after-p2)}))

(every? #(s/valid? ::card %) (:deck (game shuffled-deck)))

(s/def ::card
  (s/keys :req-un [::suite
                   ::value]))

(s/def ::suite #{:diamonds :clubs :hearts :spades})

(s/def ::value (s/or :named #{:ace :jack :queen :king}
                     :num   (s/with-gen #(and (number? %)
                                              (> % 1)
                                              (< % 11))
                              #(fn []
                                 (inc (rand-int 10))))))

(s/valid? ::card (first cards))

(s/valid? ::card {:suite 2})

(s/explain ::card (first cards))

(s/explain ::card {:suite 2})

(s/explain-data ::card {:suite 2})

(s/exercise ::card)
(s/exercise ::suite)
