checkpointer - primitive [CRaC](https://openjdk.org/projects/crac/) implementation
![CodeQL](../../actions/workflows/codeql.yaml/badge.svg)
![CI result](../../actions/workflows/ci.yaml/badge.svg)
===

checkpointer provides checkpoint/restore hooks on [CRIU](https://criu.org/) as [org.crac](https://github.com/CRaC/org.crac) interface.

# Requirements

* JDK 17 or later
* Maven
* [org.crac](https://github.com/CRaC/org.crac)
    * You can download JAR directly from [Maven Central](https://central.sonatype.com/artifact/org.crac/crac)
* [CRIU](https://criu.org/)
    * `criu` package in Fedora

# How to build

```
mvn package
```

# How to use

See [example](example).

You can build `example` with `mvn package`. Note that you have to run `mvn package` for checkpointer.

## 1. Run example app

```
# Build checkpointer
mvn package

# Build example
cd example
mvn package

# Run example
$JAVA_HOME/bin/java -javaagent:../target/checkpointer-0.1.0.jar -cp target/checkpointer-example-0.1.0.jar:../target/lib/crac-1.5.0.jar com.yasuenag.checkpointer.example.CheckpointerExample
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
* `criu` runs with `--action-script bin/checkpointer-actions.sh`. [checkpointer-actions.sh](bin/checkpointer-actions.sh) requests checkpointer to call hooks via Unix domain socket as following:
    * `pre-dump`: `beforeCheckpoint`
    * `post-resume`: `afterRestore`

# License

The GNU Lesser General Public License, version 3.0
