#!/bin/sh
basedir=$(cd `dirname $0`; pwd)
cd $basedir/../
pid=`ps -ef|grep org.datacheck.CheckScheduler|grep -v grep|grep -v PPID|awk '{ print $2}'`
if [[ $pid -gt 0 ]]
then 
echo "CheckScheduler pid" $pid" exist ,please stop it first"
exit
fi
echo "CheckScheduler Starting..."
nohup java -Xms256m -Xmx2048m -cp datacheck-0.0.1-SNAPSHOT.jar org.datacheck.CheckScheduler >/dev/null 2>&1 &
echo "CheckScheduler Started"
