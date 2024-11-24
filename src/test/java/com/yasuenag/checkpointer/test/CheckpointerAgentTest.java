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
package com.yasuenag.checkpointer.test;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    var pid = ProcessHandle.current().pid();
    var sockPath = Path.of("/tmp", String.format("checkpointer.%d", pid));

    CheckpointerAgent.premain(null);
    Assertions.assertEquals("com.yasuenag.checkpointer.crac", System.getProperty("org.crac.Core.Compat"));
    Assertions.assertTrue(sockPath.toFile().canWrite());

    var acceptorThread = Thread.getAllStackTraces()
                               .keySet()
                               .stream()
                               .filter(t -> t.getName().equals("Checkpointer Agent"))
                               .findFirst()
                               .get();
    Assertions.assertTrue(acceptorThread.isDaemon());
    Assertions.assertEquals(Thread.State.RUNNABLE, acceptorThread.getState());

    var resource = new ResourceTestImpl();
    Core.getGlobalContext().register(resource);
    var buf = ByteBuffer.allocate(1);

    // checkpoint
    try(var sock = SocketChannel.open(StandardProtocolFamily.UNIX)){
      sock.connect(UnixDomainSocketAddress.of(sockPath));

      buf.put((byte)'c');
      buf.flip();
      synchronized(resource){
        sock.write(buf);
        resource.wait();
      }
      Assertions.assertTrue(resource.isBeforeCheckpointCalled());
      Assertions.assertFalse(resource.isAfterRestoreCalled());

      buf.clear();
      sock.read(buf);
      buf.flip();
      Assertions.assertEquals((byte)'t', buf.get());
    }
    buf.clear();
    resource.clear();

    // illegal command
    try(var sock = SocketChannel.open(StandardProtocolFamily.UNIX)){
      sock.connect(UnixDomainSocketAddress.of(sockPath));
      buf.put((byte)' ');
      buf.flip();
      sock.write(buf);

      // Wait until the connection is closed by peer.
      do{
        try{
          buf.flip();
          sock.write(buf);
        }
        catch(IOException e){
          break;
        }
      }while(true);

      Assertions.assertFalse(resource.isBeforeCheckpointCalled());
      Assertions.assertFalse(resource.isAfterRestoreCalled());

      buf.clear();
      sock.read(buf);
      buf.flip();
      Assertions.assertEquals((byte)'f', buf.get());
    }
    buf.clear();

    // restore
    try(var sock = SocketChannel.open(StandardProtocolFamily.UNIX)){
      sock.connect(UnixDomainSocketAddress.of(sockPath));
      buf.put((byte)'r');
      buf.flip();
      synchronized(resource){
        sock.write(buf);
        resource.wait();
      }
      Assertions.assertFalse(resource.isBeforeCheckpointCalled());
      Assertions.assertTrue(resource.isAfterRestoreCalled());

      buf.clear();
      sock.read(buf);
      buf.flip();
      Assertions.assertEquals((byte)'t', buf.get());
    }

  }

}
