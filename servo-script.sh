#!/bin/bash

LOCKFILE="/tmp/mylockfile"
exec 200>$LOCKFILE
flock -x 200

echo "Start processing with args: $*"
servo "$@"
echo "Done."
