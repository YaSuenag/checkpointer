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

CMD=''
case "$CRTOOLS_SCRIPT_ACTION" in
  'pre-dump')
    CMD=c;;
  'post-resume')
    CMD=r;;
esac

if [ -n "$CMD" ]; then
  echo -n $CMD | nc -U /tmp/checkpointer.$CRTOOLS_INIT_PID
fi
