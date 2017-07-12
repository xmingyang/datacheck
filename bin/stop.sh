#!/bin/sh
pid=`ps -ef|grep org.datacheck.CheckScheduler|grep -v grep|grep -v PPID|awk '{ print $2}'`
if [[ $pid -gt 0 ]]
then 
echo "CheckScheduler Stopping..."
kill -9 $pid
echo "CheckScheduler Stopped"
else
echo "CheckScheduler Not Exist"
fi
