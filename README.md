# bridge

An event registration system for ClojureBridge. [Read the blog post](https://www.stuttaford.me/2018/02/18/a-clojure-learning-journey/).

## Development

### REPL / Figwheel

I use Emacs, [CIDER](https://github.com/clojure-emacs/cider), and [clj-refactor](https://github.com/clojure-emacs/clj-refactor.el), so development assumes that you have `cider-refresh` available to you.

Install the [Clojure CLI](https://clojure.org/guides/getting_started), clone this repo, and then run:

```shell
make figwheel
```

Once it's running, connect CIDER to port `7890`, and issue a `cider-refresh` (`C-c C-x`) to start the system.

Visit http://localhost:8080, and sign in with `test@cb.org` / `secret`.

### Run tests

```sh
make test
```

### Package for production

```sh
make pack
```

And then run the jar with

```sh
java -jar bridge.jar -m bridge.service
```

### Database

By default, we use an in-memory database. If you want your database to be durable, you'll need to run a [Datomic Free](https://my.datomic.com/downloads/free) transactor, and set an environment variable:

```sh
export BRIDGE_DATOMIC_URI="datomic:free://localhost:4334/bridge"
```

## Documentation

- [Requirements](doc/requirements.md).
- [Features](doc/system.md).
- [Schema](doc/schema).