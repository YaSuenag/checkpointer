/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
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
import com.yasuenag.checkpointer.crac.Core;


public class CoreTest{

  @Test
  public void testCheckpointRestore(){
    Core core = new Core();
    Assertions.assertThrows(UnsupportedOperationException.class, core::checkpointRestore);
  }

  @Test
  public void testGlobalContext(){
    Context ctxt = Core.getGlobalContext();
    Assertions.assertEquals(Context.class, ctxt.getClass());

    // Global context should be singleton.
    Context ctxt2 = Core.getGlobalContext();
    Assertions.assertSame(ctxt, ctxt2);
  }

}
