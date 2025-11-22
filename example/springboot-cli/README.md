checkpointer example for Spring Boot CLI
===

This example shows how to use checkpointer with Spring Boot.

# Build

```
export JAVA_HOME=/path/to/jdk25
mvn package
```

# Play with checkpointer

## 1. Run example app

```
$JAVA_HOME/bin/java -javaagent:/path/to/checkpointer-0.2.0.jar -jar springboot-cli-1.0.1.jar --checkpoint
```

## 2. Do checkpoint

```
mkdir /path/to/checkpoint/dir
sudo $CHECKPOINTER_REPO/bin/checkpointer.sh checkpoint [PID] /path/to/checkpoint/dir
```

## 3. Restore

```
sudo $CHECKPOINTER_REPO/bin/checkpointer.sh restore /path/to/checkpoint/dir
```

> [!NOTE]
> Set `CRIU_BIN` if you want to use specific CRIU binary e.g. `sudo env CRIU_BIN=/path/to/criu ./bin/checkpointer.sh ...`

# Ref. Normal invocation VS AOT (since Java 25) VS checkpointer

Measured application running time with `time` command.

## Running time comparison

| Execution type | Time (real) | Gain |
|---|---|---|
| Normal | 0.986s | <div align="center">-</div> |
| AOT | 0.680s | -0.306s (-31.0%) |
| checkpointer | 0.152s | -0.834 (-84.5%) |

## Measurement environment in this doc

* Fedora 43 x86\_64
    * Client Hyper-V guest
    * 4vCPU, 8GB RAM
    * kernel-6.17.4-300.fc43.x86\_64
    * glibc-2.42-4.fc43.x86\_64
* Java: java-latest-openjdk-25.0.0.0.36-0.3.fc43.x86\_64
* CRIU: Upstream (commit 2cf8f13ca)
* Hyper-V Host
    * Hardware: AMD Ryzen 3300X, 16GB RAM
    * Windows 11 25H2 (build 26200.7171)

## How to measure

### Normal invocation

```
real    0m0.986s
user    0m2.648s
sys     0m0.178s
```

```
time $JAVA_HOME/bin/java -jar springboot-cli-1.0.1.jar
```

### AOT

```
real    0m0.680s
user    0m2.089s
sys     0m0.157s
```

This pattern uses [JEP 514: Ahead-of-Time Command-Line Ergonomics](https://openjdk.org/jeps/514) introduced since Java 25. AOT cache would be generated in `package` phase in Maven. See [pom.xml](pom.xml) for details.

```
time $JAVA_HOME/bin/java -XX:AOTCache=app.aot -jar springboot-cli-1.0.1.jar
```

### checkpointer

```
real    0m0.152s
user    0m0.019s
sys     0m0.121s
```

#### 1. Start an example with `--checkpoint`

```
$JAVA_HOME/bin/java -jar springboot-cli-1.0.1.jar --checkpoint
```

#### 2. Obtain checkpoint

```
mkdir /tmp/checkpoint
sudo checkpointer.sh checkpoint [PID] /tmp/checkpoint
```

#### 3. Restore and measures processing time

```
sudo bash -c "time checkpointer.sh restore /tmp/checkpoint"
```
