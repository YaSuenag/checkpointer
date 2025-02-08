checkpointer - primitive [CRaC](https://openjdk.org/projects/crac/) implementation
![CodeQL](../../actions/workflows/codeql.yaml/badge.svg)
![CI result](../../actions/workflows/ci.yaml/badge.svg)
===

checkpointer provides checkpoint/restore hooks on [CRIU](https://criu.org/) as [org.crac](https://github.com/CRaC/org.crac) interface.

# Requirements

* JDK 8 or later
    * Unit tests require JDK 11 or later
* Maven
* [org.crac](https://github.com/CRaC/org.crac)
* [CRIU](https://criu.org/)
    * `criu` package in Fedora

# How to build

```
mvn package
```

# How to use

See [example](example).

You can build `example` with `mvn package`. Note that you have to run `mvn package` for checkpointer.

You can specify following agent option:

* `addr=<address>`: Listen address. `localhost:10095` is set by default.
* `shutdown=<true|false>`: Shutdown agent thread when restre request finished. `true` is set by default.
* `shutdown_timeout=<seconds>`: Shutdown timeout for agent thread. `10` is set by default.
* `gc=<true|false>`: `true` if invoke `System.gc()` at `beforeCheckpoint`. `false` is set by default.

You can concatenate them with `,`:

```
-javaagent:/path/to/checkpointer-0.1.0.jar=addr=localhost:10095,shutdown=true,shutdown_timeout=10
```

## 1. Run example app

```
# Build checkpointer
mvn package

# Build example
cd example
mvn package

# Run example
$JAVA_HOME/bin/java -javaagent:../target/checkpointer-0.2.0.jar -jar target/checkpointer-example-0.1.0.jar
```

## 2. Do checkpoint

```
mkdir /path/to/checkpoint/dir
sudo ./bin/checkpointer.sh checkpoint [PID] /path/to/checkpoint/dir
```

## 3. Restore

```
sudo ./bin/checkpointer.sh restore /path/to/checkpoint/dir
```

# Under the hood

* checkpointer provides `org.crac` implementation especially `Resource` and `Context`. You can create your `beforeCheckpoint` and `afterRestore` hook.
* checkpointer creates HTTP endpoint to accept hook request from externals.
* `criu` runs with `--action-script bin/checkpointer-actions.sh`. [checkpointer-actions.sh](bin/checkpointer-actions.sh) requests checkpointer to call hooks via REST API on checkpointer Java Agent:
    * `pre-dump`: `beforeCheckpoint`
    * `post-resume`: `afterRestore`

# License

The GNU Lesser General Public License, version 3.0
