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
cd example
$JAVA_HOME/bin/java -XX:-UsePerfData -javaagent:/path/to/checkpointer-0.1.0.jar -cp target/classes:$HOME/.m2/repository/org/crac/crac/1.5.0/crac-1.5.0.jar com.yasuenag.checkpointer.example.CheckpointerExample
```

## 2. Do checkpoint

Note that you cannot see the PID on `jcmd` because the process runs with `-XX:-UsePerfData`.

```
mkdir /tmp/checkpoint
sudo criu dump -t [PID] --external unix --action-script /path/to/checkpointer/bin/checkpointer-actions.sh -D /tmp/checkpoint -j
```

## 3. Restore

```
sudo criu restore --action-script /path/to/bin/checkpointer-actions.sh -D /tmp/checkpoint -j
```

# Under the hood

* checkpointer provides `org.crac` implementation especially `Resource` and `Context`. You can create your `beforeCheckpoint` and `afterRestore` hook.
* checkpointer creates Unix domain socket as `/tmp/checkpointer.[PID]`. checkpointer accepts hook request from externals via this socket.
* `criu` runs with `--action-script bin/checkpointer-actions.sh`. [checkpointer-actions.sh](bin/checkpointer-actions.sh) requests checkpointer to call hooks via Unix domain socket as following:
    * `pre-dump`: `beforeCheckpoint`
    * `post-resume`: `afterRestore`

# Known issues / TODO

* Cannot handle `hsperfdata` - need to disable `-XX:UsePerfData`
    * [CRaC JDK](https://github.com/openjdk/crac) remaps memory segments for hsperfdata in HotSpot.
* Need to block event hooks
    * [checkpointer-actions.sh](bin/checkpointer-actions.sh) have to block until completion of each hooks.
* Specify path to unix domain socket without wild card in [checkpointer-actions.sh](bin/checkpointer-actions.sh)
* Improve error handlings
* Improve examples
    * Download dependencies to run easily.
* Publish checkpointer into GitHub Packages
    * Create GHA workflow.

# License

The GNU Lesser General Public License, version 3.0
