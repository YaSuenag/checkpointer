checkpointer example for Spring Boot CLI
===

This example shows how to use checkpointer with Spring Boot.

> [!IMPORTANT]
> Spring Framework v6.2.2 on which Spring Boot v3.4.2 depends has a potential bug not to finish the app after restoring.
> See [Pull Request on Spring Framework](https://github.com/spring-projects/spring-framework/pull/34372) for detailes. You can get a patch to resolve the issue.

# Build

```
mvn package
```

# Play with checkpointer

## 1. Run example app

```
$JAVA_HOME/bin/java -javaagent:/path/to/checkpointer-0.2.0.jar -jar springboot-cli-1.0.0.jar
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
