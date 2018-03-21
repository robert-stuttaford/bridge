(ns bridge.data.slug-test
  (:require [bridge.data.slug :as slug]
            [clojure.test :refer [deftest is testing]]))

(deftest ->slug
  (testing "Simple slugify"
    (is (= "charlie-brown" (slug/->slug "charlie brown")))
    (is (= "charlie-brown" (slug/->slug "Charlie Brown"))))
  (testing "Unecessary spaces"
    (is (= "charlie-brown" (slug/->slug "    Charlie Brown    "))))
  (testing "Delimiter and trim value"
    (is (= "charlie-brown-peppermint-patty-and-lucy-van-pelt"
           (slug/->slug "Charlie Brown, Peppermint Patty and Lucy van Pelt"))))
  (testing "Nonascii chars"
    (is (= "aaaaaaaaaa" (slug/->slug "áÁàÀãÃâÂäÄ")))
    (is (= "eeeeeeeeee" (slug/->slug "éÉèÈẽẼêÊëË")))
    (is (= "iiiiiiiiii" (slug/->slug "íÍìÌĩĨîÎïÏ")))
    (is (= "oooooooooo" (slug/->slug "óÓòÒõÕôÔöÖ")))
    (is (= "uuuuuuuuuu" (slug/->slug "úÚùÙũŨûÛüÜ")))
    (is (= "cccccc" (slug/->slug "ćĆĉĈçÇ")))))
