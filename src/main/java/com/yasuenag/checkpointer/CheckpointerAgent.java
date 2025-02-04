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
package com.yasuenag.checkpointer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.yasuenag.checkpointer.crac.Context;
import com.yasuenag.checkpointer.crac.Core;


public class CheckpointerAgent implements HttpHandler{

  public static final String BEFORE_CHECKPOINT_PATH = "/before-checkpoint";
  public static final String AFTER_RESTORE_PATH = "/after-restore";

  private final ExecutorService tpForServer;

  private final HttpServer server;

  private InetSocketAddress addr;

  private boolean needShutdown;

  private int serverShutdownTimeout;

  private void parseOptions(String options){
    String host = "localhost";
    int port = 10095;
    needShutdown = true;
    serverShutdownTimeout = 10;

    if(options != null){
      for(String option : options.split(",")){
        String[] keyval = option.split("=");
        switch(keyval[0]){
          case "addr":
            String[] hostport = keyval[1].split(":");
            if(hostport.length > 2){
              throw new IllegalArgumentException("Illegal address: " + keyval[1]);
            }
            host = hostport[0];
            port = (hostport.length == 2) ? Integer.parseInt(hostport[1]) : 10095;
            break;

          case "shutdown":
            needShutdown = Boolean.parseBoolean(keyval[1]);
            break;

          case "shutdown_timeout":
            serverShutdownTimeout = Integer.parseInt(keyval[1]);
            break;
        }
      }
    }

    addr = new InetSocketAddress(host, port);
  }

  public CheckpointerAgent(String options) throws IOException{
    parseOptions(options);

    tpForServer = Executors.newFixedThreadPool(1);
    server = HttpServer.create(addr, 0);
    server.setExecutor(tpForServer);
    server.createContext(BEFORE_CHECKPOINT_PATH, this);
    server.createContext(AFTER_RESTORE_PATH, this);

    server.start();
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException{
    int rCode = 400;
    boolean doShutdown = false;

    try{
      if(exchange.getRequestMethod().equals("POST")){
        switch(exchange.getHttpContext().getPath()){
          case BEFORE_CHECKPOINT_PATH:
            Context.runAllOfBeforeCheckpointHooks();
            rCode = 204;
            break;

          case AFTER_RESTORE_PATH:
            Context.runAllOfAfterRestoreHooks();
            rCode = 204;
            doShutdown = needShutdown;
            break;
        }
      }

      exchange.sendResponseHeaders(rCode, -1);
    }
    catch(IOException e){
      throw e;
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
    finally{
      exchange.close();

      if(doShutdown){
        server.stop(serverShutdownTimeout);
        tpForServer.shutdown();
      }
    }
  }

  public void shutdown(){
    server.stop(serverShutdownTimeout);
    tpForServer.shutdown();
  }

  public static void agentmain(String agentArgs) throws Exception{
    premain(agentArgs);
  }

  public static void premain(String agentArgs) throws Exception{
    System.setProperty("org.crac.Core.Compat", Core.class.getPackage().getName());

    new CheckpointerAgent(agentArgs);
  }

}
