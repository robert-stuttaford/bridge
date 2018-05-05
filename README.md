# bridge

An event registration system for ClojureBridge. [Read the blog post](https://www.stuttaford.me/2018/02/18/a-clojure-learning-journey/).

Dev diary:

- [Bridge dev diary: Datomic mode](https://www.stuttaford.me/2018/03/03/bridge-dev-diary--datomic-mode/) — 3 March, 2018
- [Bridge dev diary: Modelling access](https://www.stuttaford.me/2018/03/17/bridge-dev-diary--modelling-access/) — 17 March, 2018
- [Bridge dev diary: Events API](https://www.stuttaford.me/2018/03/31/bridge-dev-diary--events-api/) — 31 March, 2018
- [Bridge dev diary: Events UI](https://www.stuttaford.me/2018/04/28/bridge-dev-diary--events-ui/) — 28 April, 2018
- [Bridge dev diary: Events UI — Paying some tech debt](https://www.stuttaford.me/2018/05/05/bridge-dev-diary--events-ui-paying-some-tech-debt/) — 5 May, 2018

## Development

Install the [Clojure CLI](https://clojure.org/guides/getting_started), clone this repo.

### Try it out

To build the Cljs and run the web server:

```shell
make compile serve
```

Visit http://localhost:8080, and sign in with `test@cb.org` / `secret`.

### Interactive development with the REPL and Figwheel

I use Emacs, [CIDER](https://github.com/clojure-emacs/cider), and [clj-refactor](https://github.com/clojure-emacs/clj-refactor.el), so I assume that you have `cider-refresh` available to you.

```shell
make figwheel
```

Once it's running, connect CIDER to port `7890`, and issue a `cider-refresh` (`C-c C-x`) to start the system.

### Run tests

```sh
make test
```

### Package for production

```sh
make pack
```

And then run the jar:

```sh
java -jar bridge.jar -m bridge.service
```

### Database

By default, we use an in-memory database.

If you want your database to be durable, you'll need to run a [Datomic Free](https://my.datomic.com/downloads/free) transactor, and set an environment variable:

```sh
export BRIDGE_DATOMIC_URI="datomic:free://localhost:4334/bridge"
```


## Documentation

- [Requirements](doc/requirements.md).
- [Features](doc/system.md).
- [Schema](doc/schema.md).


## License

Copyright © Robert Stuttaford

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
