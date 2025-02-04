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
package com.yasuenag.checkpointer.crac;

import java.util.HashSet;
import java.util.Set;


public class Context extends org.crac.Context<Resource>{

  private static final Set<Context> CONTEXTS = new HashSet<>();

  private final Set<Resource> resources;

  public Context(){
    CONTEXTS.add(this);
    resources = new HashSet<>();
  }

  @Override
  public void beforeCheckpoint(org.crac.Context<? extends org.crac.Resource> context) throws org.crac.CheckpointException{
    for(Resource resource : resources){
      try{
        resource.beforeCheckpoint(context);
      }
      catch(Exception e){
        throw new CheckpointException(e);
      }
    }
  }

  @Override
  public void afterRestore(org.crac.Context<? extends org.crac.Resource> context) throws org.crac.RestoreException{
    for(Resource resource : resources){
      try{
        resource.afterRestore(context);
      }
      catch(Exception e){
        throw new RestoreException(e);
      }
    }
  }

  @Override
  public void register(Resource resource){
    resources.add(resource);
  }

  public static void runAllOfBeforeCheckpointHooks() throws CheckpointException{
    try{
      for(Context ctxt : CONTEXTS){
        for(Resource res : ctxt.resources){
          res.beforeCheckpoint(ctxt);
        }
      }
    }
    catch(Exception e){
      throw new CheckpointException(e);
    }
  }

  public static void runAllOfAfterRestoreHooks() throws RestoreException{
    try{
      for(Context ctxt : CONTEXTS){
        for(Resource res : ctxt.resources){
          res.afterRestore(ctxt);
        }
      }
    }
    catch(Exception e){
      throw new RestoreException(e);
    }
  }

}
