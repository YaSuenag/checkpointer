#!/bin/bash

# Copyright (C) 2024, 2025, Yasumasa Suenaga
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

  TARGET_USER=`sed -e 's/\x0/\n/g' /proc/$TARGET_PID/environ | grep -w USER | cut -d '=' -f 2`
  echo $TARGET_USER > $CPDIR/username
  echo $TARGET_PID > $CPDIR/target_pid
  criu dump -t $TARGET_PID --action-script $ACTION_SCRIPT -D $CPDIR -j
  mv /tmp/hsperfdata_$TARGET_USER/$TARGET_PID $CPDIR/hsperfdata
  rm -f /tmp/checkpointer.$TARGET_PID
elif [ "$CMD" == 'restore' ]; then
  CPDIR=$ARG1
  if [ -z "$CPDIR" ]; then
    echo 'Checkpoint directory is empty'
    exit 2
  fi

  TARGET_USER=`cat $CPDIR/username`
  TARGET_PID=`cat $CPDIR/target_pid`
  mkdir -p /tmp/hsperfdata_$TARGET_USER
  cp $CPDIR/hsperfdata /tmp/hsperfdata_$TARGET_USER/$TARGET_PID
  chown $TARGET_USER /tmp/hsperfdata_$TARGET_USER/$TARGET_PID
  criu restore --action-script $ACTION_SCRIPT -D $CPDIR -j
elif [ -z "$CMD" ]; then
    echo 'Command is empty'
    exit 100
else
    echo "Unknown command: $CMD"
    exit 200
fi
