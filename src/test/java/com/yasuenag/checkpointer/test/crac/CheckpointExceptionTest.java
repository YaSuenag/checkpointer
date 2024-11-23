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

import com.yasuenag.checkpointer.crac.CheckpointException;


public class CheckpointExceptionTest{

  @Test
  public void testCheckpointExceptionConstructorWithThrowable(){
    var causeException = new Exception("cause");
    var testException = new CheckpointException(causeException);

    Assertions.assertEquals(causeException.getMessage(), testException.getCause().getMessage());
  }

}
