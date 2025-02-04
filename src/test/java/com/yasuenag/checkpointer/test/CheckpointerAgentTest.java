/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
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
package com.yasuenag.checkpointer.test;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.yasuenag.checkpointer.CheckpointerAgent;


public class CheckpointerAgentTest{

  public static class ResourceTestImpl implements Resource{

    private boolean beforeCheckpointCalled = false;
    private boolean afterRestoreCalled = false;

    public synchronized void beforeCheckpoint(Context<? extends Resource> context) throws Exception{
      beforeCheckpointCalled = true;
      this.notify();
    }

    public synchronized void afterRestore(Context<? extends Resource> context) throws Exception{
      afterRestoreCalled = true;
      this.notify();
    }

    public void clear(){
      beforeCheckpointCalled = false;
      afterRestoreCalled = false;
    }

    public boolean isBeforeCheckpointCalled(){
      return beforeCheckpointCalled;
    }

    public boolean isAfterRestoreCalled(){
      return afterRestoreCalled;
    }

  }

  @Test
  public void testCheckpointerAgent() throws Exception{
    CheckpointerAgent.premain("shutdown=false");
    Assertions.assertEquals("com.yasuenag.checkpointer.crac", System.getProperty("org.crac.Core.Compat"));

    // Sleep 3 sec to wait to start HTTP server...
    Thread.sleep(3000);

    ResourceTestImpl resource = new ResourceTestImpl();
    Core.getGlobalContext().register(resource);

    HttpClient client = HttpClient.newBuilder()
                                  .version(HttpClient.Version.HTTP_1_1)
                                  .build();

    HttpRequest request;
    HttpResponse<Void> response;

    // checkpoint
    request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:10095/before-checkpoint"))
                         .POST(HttpRequest.BodyPublishers.noBody())
                         .build();
    response = client.send(request, HttpResponse.BodyHandlers.discarding());
    Assertions.assertEquals(204, response.statusCode());

    // illegal command
    request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:10095/silver-bullet"))
                         .POST(HttpRequest.BodyPublishers.noBody())
                         .build();
    response = client.send(request, HttpResponse.BodyHandlers.discarding());
    Assertions.assertEquals(404, response.statusCode());

    // restore
    request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:10095/after-restore"))
                         .POST(HttpRequest.BodyPublishers.noBody())
                         .build();
    response = client.send(request, HttpResponse.BodyHandlers.discarding());
    Assertions.assertEquals(204, response.statusCode());
  }

  public static Stream<String> addrProvider() throws Exception{
    String hostname = InetAddress.getLocalHost().getHostName();
    return Stream.of(hostname,
                     hostname + ":10000",
                     "localhost",
                     "localhost:10001");
  }

  @ParameterizedTest
  @MethodSource("addrProvider")
  public void testAddrOption(String addr) throws Exception{
    CheckpointerAgent agent = new CheckpointerAgent("addr=" + addr);
    if(!addr.contains(":")){
      addr += ":10095";
    }

    // Sleep 3 sec to wait to start HTTP server...
    Thread.sleep(3000);

    ResourceTestImpl resource = new ResourceTestImpl();
    Core.getGlobalContext().register(resource);

    HttpClient client = HttpClient.newBuilder()
                                  .version(HttpClient.Version.HTTP_1_1)
                                  .build();

    // checkpoint
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create("http://" + addr + "/before-checkpoint"))
                                     .POST(HttpRequest.BodyPublishers.noBody())
                                     .build();
    HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

    Assertions.assertEquals(204, response.statusCode());

    agent.shutdown();
  }

  @Test
  public void testNoShutdown() throws Exception{
    CheckpointerAgent agent = new CheckpointerAgent("shutdown=false");

    // Sleep 3 sec to wait to start HTTP server...
    Thread.sleep(3000);

    ResourceTestImpl resource = new ResourceTestImpl();
    Core.getGlobalContext().register(resource);

    HttpClient client = HttpClient.newBuilder()
                                  .version(HttpClient.Version.HTTP_1_1)
                                  .build();

    HttpRequest request;
    HttpResponse<Void> response;

    // restore
    request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:10095/after-restore"))
                         .POST(HttpRequest.BodyPublishers.noBody())
                         .build();
    response = client.send(request, HttpResponse.BodyHandlers.discarding());
    Assertions.assertEquals(204, response.statusCode());

    // Sleep 3 sec...
    Thread.sleep(3000);

    // restore (2)
    request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:10095/after-restore"))
                         .POST(HttpRequest.BodyPublishers.noBody())
                         .build();
    response = client.send(request, HttpResponse.BodyHandlers.discarding());
    Assertions.assertEquals(204, response.statusCode());

    agent.shutdown();
  }

  @Test
  public void testShutdownTimeout() throws Exception{
    CheckpointerAgent agent = new CheckpointerAgent("shutdown_timeout=60");
    agent.shutdown();
  }

  @Test
  public void testOptionConbination() throws Exception{
    CheckpointerAgent agent = new CheckpointerAgent("addr=localhost:10002,shutdown=false,shutdown_timeout=60");
    agent.shutdown();
  }

}
