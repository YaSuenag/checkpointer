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
package com.yasuenag.checkpointer;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import com.yasuenag.checkpointer.crac.Context;
import com.yasuenag.checkpointer.crac.Core;


public class CheckpointerAgent implements Runnable{

  private final ServerSocketChannel serverCh;

  public CheckpointerAgent() throws IOException{
    var pid = ProcessHandle.current().pid();
    var sockPath = Path.of("/tmp", String.format("checkpointer.%d", pid));

    serverCh = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
    serverCh.bind(UnixDomainSocketAddress.of(sockPath));
    sockPath.toFile().deleteOnExit();
  }

  public void startAcceptorThread(){
    var acceptorThread = new Thread(this, "Checkpointer Agent");
    acceptorThread.setDaemon(true);
    acceptorThread.start();
  }

  @Override
  public void run(){
    try(serverCh){
      SocketChannel ch;
      var buf = ByteBuffer.allocate(1);
      while((ch = serverCh.accept()) != null){
        boolean result = false;
        try{
          buf.clear();
          ch.read(buf);
          buf.flip();
          byte cmd = buf.get();

          if(cmd == (byte)'c'){ // checkpoint
            Context.runAllOfBeforeCheckpointHooks();
          }
          else if(cmd == (byte)'r'){ // restore
            Context.runAllOfAfterRestoreHooks();
          }
          else{
            throw new RuntimeException("Illegal command: " + (char)cmd);
          }
          result = true;
        }
        catch(Exception e){
          e.printStackTrace();
        }
        finally{
          buf.clear();
          buf.put(result ? (byte)'t' : (byte)'f');
          buf.flip();
          ch.write(buf);
          ch.close();
        }
      }
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void agentmain(String agentArgs) throws Exception{
    premain(agentArgs);
  }

  public static void premain(String agentArgs) throws Exception{
    System.setProperty("org.crac.Core.Compat", Core.class.getPackageName());
    var agent = new CheckpointerAgent();
    agent.startAcceptorThread();
  }

}
