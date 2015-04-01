#!/bin/bash
echo " Current module will shutdown ..."
pid=`cat process.pid | awk 'echo $1'`
echo "pid:$pid"
kill -9 $pid
rm -f process.pid
echo "shutdown ok."
exit 0