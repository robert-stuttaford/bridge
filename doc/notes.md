# on usernames

https://www.b-list.org/weblog/2018/feb/11/usernames/

cliff notes:
- be case insensitive
- use 3 identifiers
  - internal id - ours is the datomic entity id
  - public id - a display name
  - auth id - only used to sign in - ours is email
- normalise all inputs
  - `(java.text.Normalizer/normalize s java.text.Normalizer$Form/NFC)`

# generate emails

Joel Sánchez @JoelSanchezDev

```clojure
(spec/def :user/email
  (spec/with-gen
    (spec/and string? #(str/includes? % "@"))
    #(chuck/string-from-regex #"[a-z0-9]{3,6}@[a-z0-9]{3,6}\.(com|es|org)")))
```

`[com.gfredericks/test.chuck "0.2.8"]`

# depstar

https://github.com/healthfinch/depstar instead of pack.alpha?

# datomic layer

- automatically track all calls for a request, and present this info in the response
- interceptors

# client

evaluate re-frame
testing https://github.com/Day8/re-frame-test

10x function tracing https://github.com/Day8/re-frame-debux#how-to-use

# graphviz

http://stevebuik.github.io/GraphVizForce/

https://github.com/xsc/rewrite-clj
