package com.yasuenag.presentation.ntttechconf2025;

import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Component;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;


@Component
public class CheckpointCoordinator implements Resource{

  private final CountDownLatch latch;

  public CheckpointCoordinator(){
    latch = new CountDownLatch(1);
    Core.getGlobalContext().register(this);
  }

  @Override
  public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
    System.out.println("<< beforeCheckpoint >>");
  }

  @Override
  public void afterRestore(Context<? extends Resource> context) throws Exception {
    System.out.println("<< afterRestore >>");
    latch.countDown();
  }

  public void await() throws InterruptedException{
    latch.await();
  }

}
