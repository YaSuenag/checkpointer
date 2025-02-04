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

CHECKPOINTER_HOST=localhost:10095
CMD=''
case "$CRTOOLS_SCRIPT_ACTION" in
  'pre-dump')
    CMD='before-checkpoint';;
  'post-resume')
    CMD='after-restore';;
esac

if [ -n "$CMD" ]; then
  RESULT=`curl -XPOST -s -w "%{http_code}" http://$CHECKPOINTER_HOST/$CMD`
  if [ $RESULT -eq 204 ]; then
    exit 0
  else
    exit 1
  fi
fi
