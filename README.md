checkpointer - primitive [CRaC](https://openjdk.org/projects/crac/) implementation
===

checkpointer provides checkpoint/restore hooks on [CRIU](https://criu.org/) as [org.crac](https://github.com/CRaC/org.crac) interface.

# Requirements

* JDK 17 or later
* Maven
* [org.crac](https://github.com/CRaC/org.crac)
* [CRIU](https://criu.org/)
    * `criu` package in Fedora
* [Ncat](https://nmap.org/ncat/)
    * `nmap-ncat` package in Fedora

# How to build

```
mvn package
```

# How to use

See [example](example).

You can build `example` with `mvn package`. Note that you have to run `mvn install` for checkpointer.

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

Note that you cannot see the PID on `jcmd` because the process runs with `-XX:-UsePerfData`.

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
* checkpointer creates Unix domain socket as `/tmp/checkpointer.[PID]`. checkpointer accepts hook request from externals via this socket.
* `criu` runs with `--action-script bin/checkpointer-actions.sh`. [checkpointer-actions.sh](bin/checkpointer-actions.sh) requests checkpointer to call hooks via Unix domain socket as following:
    * `pre-dump`: `beforeCheckpoint`
    * `post-resume`: `afterRestore`

# Known issues / TODO

* Publish checkpointer into GitHub Packages
    * Create GHA workflow.

# License

The GNU Lesser General Public License, version 3.0
