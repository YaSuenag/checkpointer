checkpointer example for Spring Boot CLI
===

This example shows how to use checkpointer with Spring Boot.

> [!IMPORTANT]
> Spring Framework v6.2.2 on which Spring Boot v3.4.2 depends has a potential bug not to finish the app after restoring.
> See [Pull Request on Spring Framework](https://github.com/spring-projects/spring-framework/pull/34372) for detailes. You can get a patch to resolve the issue.

# Build

```
export JAVA_HOME=/path/to/jdk24
mvn package
```

# Play with checkpointer

## 1. Run example app

```
$JAVA_HOME/bin/java -javaagent:/path/to/checkpointer-0.2.0.jar -jar springboot-cli-1.0.0.jar --checkpoint
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

# Ref. Normal invocation VS AOT (since Java 24) VS checkpointer

Measured application running time with `time` command.

## Running time comparison

| Execution type | Time (real) | Gain |
|---|---|---|
| Normal | 1.012s | <div align="center">-</div> |
| AOT | 0.950s | -0.062s (6.12%) |
| checkpointer | 0.356s | -0.656 (64.82%) |

## Measurement environment in this doc

* Fedora 41 x86\_64
    * Client Hyper-V guest
    * 4vCPU, 8GB RAM
    * kernel-6.13.8-200.fc41.x86\_64
    * glibc-2.40-23.fc41.x86\_64
* Java: java-latest-openjdk-24.0.0.0.36-1.rolling.fc41.x86\_64
* CRIU: criu-4.0-4.fc41.x86\_64
* Hyper-V Host
    * Hardware: AMD Ryzen 3300X, 16GB RAM
    * Windows 11 24H2 (build 26100.3476)

## How to measure

### Normal invocation

```
real    0m1.012s
user    0m2.562s
sys     0m0.215s
```

```
time $JAVA_HOME/bin/java -jar springboot-cli-1.0.0.jar
```

### AOT

```
real    0m0.950s
user    0m2.525s
sys     0m0.190s
```

This pattern uses [JEP 483: Ahead-of-Time Class Loading & Linking](https://openjdk.org/jeps/483) introduced since Java 24. AOT cache would be generated in `package` phase in Maven. See [pom.xml](pom.xml) for details.

```
time $JAVA_HOME/bin/java -XX:AOTCache=app.aot -jar springboot-cli-1.0.0.jar
```

### checkpointer

```
real    0m0.356s
user    0m0.055s
sys     0m0.124s
```

#### 1. Start an example with `--checkpoint`

```
$JAVA_HOME/bin/java -jar springboot-cli-1.0.0.jar --checkpoint
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
