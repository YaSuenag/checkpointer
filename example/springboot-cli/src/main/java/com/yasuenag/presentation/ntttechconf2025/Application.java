package com.yasuenag.presentation.ntttechconf2025;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Application implements ApplicationRunner{

  private final CheckpointCoordinator cpCoordinator;

  @Autowired
  public Application(CheckpointCoordinator cpCoordinator){
    this.cpCoordinator = cpCoordinator;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if(args.containsOption("checkpoint")){
      System.out.println("Ready to obtain checkpoint...");
      // Wait restoring...
      cpCoordinator.await();
    }
    System.out.println("from Spring Boot App");
  }

}
