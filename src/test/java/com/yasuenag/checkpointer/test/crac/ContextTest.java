/*
 * Copyright (C) 2024, Yasumasa Suenaga
 *
 * This file is part of checkpointer                                     *
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
 * along with checkpointer.  If not, see <http://www.gnu.org/licenses/>.*/
package com.yasuenag.checkpointer.test.crac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yasuenag.checkpointer.crac.Context;
import com.yasuenag.checkpointer.crac.Resource;


public class ContextTest{

  public static class ResourceTest implements Resource{

    private final org.crac.Context<? extends org.crac.Resource> callerContext;

    private boolean beforeCheckpointCalled = false;
    private boolean afterRestoreCalled = false;

    public ResourceTest(org.crac.Context<? extends org.crac.Resource> callerContext){
      this.callerContext = callerContext;
    }

    @Override
    public void beforeCheckpoint(org.crac.Context<? extends org.crac.Resource> context) throws Exception{
      Assertions.assertEquals(callerContext, context);
      beforeCheckpointCalled = true;
    }

    @Override
    public void afterRestore(org.crac.Context<? extends org.crac.Resource> context) throws Exception{
      Assertions.assertEquals(callerContext, context);
      afterRestoreCalled = true;
    }

    public boolean isBeforeCheckpointCalled(){
      return beforeCheckpointCalled;
    }

    public boolean isAfterRestoreCalled(){
      return afterRestoreCalled;
    }

  }

  @Test
  public void testBeforeCheckpoint() throws Exception{
    var ctxt = new Context();
    var resource = new ResourceTest(ctxt);
    ctxt.register(resource);

    ctxt.beforeCheckpoint(ctxt);
    Assertions.assertTrue(resource.isBeforeCheckpointCalled());
    Assertions.assertFalse(resource.isAfterRestoreCalled());
  }

  @Test
  public void testAfterRestore() throws Exception{
    var ctxt = new Context();
    var resource = new ResourceTest(ctxt);
    ctxt.register(resource);

    ctxt.afterRestore(ctxt);
    Assertions.assertFalse(resource.isBeforeCheckpointCalled());
    Assertions.assertTrue(resource.isAfterRestoreCalled());
  }

  @Test
  public void testRunAllOfBeforeCheckpointHooks() throws Exception{
    var ctxt = new Context();
    var resource = new ResourceTest(ctxt);
    ctxt.register(resource);

    var ctxt2 = new Context();
    var resource2 = new ResourceTest(ctxt2);
    ctxt2.register(resource2);

    Context.runAllOfBeforeCheckpointHooks();

    Assertions.assertTrue(resource.isBeforeCheckpointCalled());
    Assertions.assertFalse(resource.isAfterRestoreCalled());
    Assertions.assertTrue(resource2.isBeforeCheckpointCalled());
    Assertions.assertFalse(resource2.isAfterRestoreCalled());
  }

  @Test
  public void testRunAllOfAfterRestoreHooks() throws Exception{
    var ctxt = new Context();
    var resource = new ResourceTest(ctxt);
    ctxt.register(resource);

    var ctxt2 = new Context();
    var resource2 = new ResourceTest(ctxt2);
    ctxt2.register(resource2);

    Context.runAllOfAfterRestoreHooks();

    Assertions.assertFalse(resource.isBeforeCheckpointCalled());
    Assertions.assertTrue(resource.isAfterRestoreCalled());
    Assertions.assertFalse(resource2.isBeforeCheckpointCalled());
    Assertions.assertTrue(resource2.isAfterRestoreCalled());
  }

}
