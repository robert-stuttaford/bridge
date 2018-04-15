test datomic connection failures - does holding on to a conn work?

docstrings for all engine code
plug in http://gdeer81.github.io/marginalia/ / https://github.com/gdeer81/lein-marginalia/blob/master/src/leiningen/marg.clj

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

http://ingesolvoll.github.io/posts/2018-04-01-learning-kee-frame-in-5-minutes/
http://ingesolvoll.github.io/posts/2018-04-01-kee-frame-putting-the-url-in-charge/

testing https://github.com/Day8/re-frame-test

10x function tracing https://github.com/Day8/re-frame-debux#how-to-use

# testing

https://github.com/walmartlabs/test-reporting

# graphviz

http://stevebuik.github.io/GraphVizForce/

# misc

https://github.com/xsc/rewrite-clj
