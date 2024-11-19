/*
 * Copyright (C) 2024, Yasumasa Suenaga
 *
 * This file is part of checkpointer
 *
 * checkpointer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * checkpointer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with checkpointer.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.yasuenag.checkpointer.example;

import java.util.concurrent.Semaphore;
import java.time.LocalDateTime;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;


public class CheckpointerExample implements Resource{

  private final Semaphore cpSemaphore;

  public CheckpointerExample(){
    cpSemaphore = new Semaphore(1, true);
    Core.getGlobalContext().register(this);
  }

  @Override
  public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
    cpSemaphore.acquire();
    System.out.println("<< beforeCheckpoint >>");
  }

  @Override
  public void afterRestore(Context<? extends Resource> context) throws Exception {
    System.out.println("<< afterRestore >>");
    cpSemaphore.release();
  }

  public void run() throws Exception{
    int cnt = 0;
    while(true){
      cpSemaphore.acquire();
      {
        System.out.printf("%s: %d\n", LocalDateTime.now().toString(), cnt++);
        Thread.sleep(1000);
      }
      cpSemaphore.release();
    }
  }

  public static void main(String[] args) throws Exception{
    var inst = new CheckpointerExample();
    inst.run();
  }

}
