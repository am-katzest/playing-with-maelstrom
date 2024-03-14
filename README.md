playing with [maelstrom](https://github.com/jepsen-io/maelstrom)

look at my children, working so hard to increment a number together 🥹

to launch [crdt](https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type) ones (pn-counter/g-counter/g-set):

``` sh
maelstrom test -w pn-counter --bin spread_crdt.bb pn-counter
```

to run tests:

``` sh
bb test_runner.clj
```

`
