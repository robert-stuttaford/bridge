normalise all inputs `(java.text.Normalizer/normalize s java.text.Normalizer$Form/NFC)`

# generate emails

Joel Sánchez @JoelSanchezDev

`[com.gfredericks/test.chuck "0.2.8"]`

```clojure
(spec/def :user/email
  (spec/with-gen
    (spec/and string? #(str/includes? % "@"))
    #(chuck/string-from-regex #"[a-z0-9]{3,6}@[a-z0-9]{3,6}\.(com|es|org)")))
```

# depstar

https://github.com/healthfinch/depstar instead of pack.alpha?

# datomic layer

- automatically track all calls for a request, and present this info in the response
- interceptors

# client

use https://github.com/ingesolvoll/kee-frame for its routing and event chains?

testing https://github.com/Day8/re-frame-test

10x function tracing https://github.com/Day8/re-frame-debux#how-to-use

# graphviz

http://stevebuik.github.io/GraphVizForce/

# misc

https://github.com/xsc/rewrite-clj
