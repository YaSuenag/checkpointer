#!/bin/bash

# Copyright (C) 2024, Yasumasa Suenaga
#
# This file is part of checkpointer
#
# checkpointer is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# checkpointer is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with checkpointer.  If not, see <http://www.gnu.org/licenses/>.

CMD=$1
ARG1=$2
ARG2=$3

BASEDIR=$(realpath `dirname $0`)
ACTION_SCRIPT=$BASEDIR/checkpointer-actions.sh

if [ "$CMD" == 'checkpoint' ]; then
  TARGET_PID=$ARG1
  if [ -z "$TARGET_PID" ]; then
    echo 'PID is empty'
    exit 1
  fi

  CPDIR=$ARG2
  if [ -z "$CPDIR" ]; then
    echo 'Checkpoint directory is empty'
    exit 2
  fi

  criu dump -t $TARGET_PID --external unix --action-script $ACTION_SCRIPT -D $CPDIR -j
elif [ "$CMD" == 'restore' ]; then
  CPDIR=$ARG1
  if [ -z "$CPDIR" ]; then
    echo 'Checkpoint directory is empty'
    exit 2
  fi

  criu restore --action-script $ACTION_SCRIPT -D $CPDIR -j
elif [ -z "$CMD" ]; then
    echo 'Command is empty'
    exit 100
else
    echo "Unknown command: $CMD"
    exit 200
fi
