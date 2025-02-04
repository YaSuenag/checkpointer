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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.yasuenag.checkpointer.crac.Context;
import com.yasuenag.checkpointer.crac.Core;


public class CheckpointerAgent implements HttpHandler{

  public static final String BEFORE_CHECKPOINT_PATH = "/before-checkpoint";
  public static final String AFTER_RESTORE_PATH = "/after-restore";

  private final HttpServer server;

  public CheckpointerAgent(String option) throws IOException{
    String addr = "localhost:10095";
    if(option != null){
      if(option.startsWith("addr=")){
        addr = option.substring(5);
      }
      else{
        throw new IllegalArgumentException("Unknown option: " + option);
      }
    }

    String[] hostport = addr.split(":");
    if(hostport.length > 2){
      throw new IllegalArgumentException("Illegal address: " + addr);
    }

    int port = (hostport.length == 2) ? Integer.parseInt(hostport[1]) : 10095;
    InetSocketAddress sockAddr = new InetSocketAddress(hostport[0], port);
    server = HttpServer.create(sockAddr, 0);
    server.setExecutor(Executors.newFixedThreadPool(1));
    server.createContext(BEFORE_CHECKPOINT_PATH, this);
    server.createContext(AFTER_RESTORE_PATH, this);

    server.start();
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException{
    int rCode = 400;

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
    }
  }

  public static void agentmain(String agentArgs) throws Exception{
    premain(agentArgs);
  }

  public static void premain(String agentArgs) throws Exception{
    System.setProperty("org.crac.Core.Compat", Core.class.getPackage().getName());

    new CheckpointerAgent(agentArgs);
  }

}
