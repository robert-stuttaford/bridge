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

