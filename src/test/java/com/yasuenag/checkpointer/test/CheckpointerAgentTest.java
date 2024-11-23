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

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yasuenag.checkpointer.CheckpointerAgent;


public class CheckpointerAgentTest{

  @Test
  public void testSysProp() throws Exception{
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
  }

}
