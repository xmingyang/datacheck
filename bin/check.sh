#!/bin/sh
basedir=$(cd `dirname $0`; pwd)
cd $basedir
source /home/hdp-ads-audit/.bash_profile
pidcnt=`ps -ef | grep org.datacheck.CheckScheduler | grep -v grep | wc -l`
echo $pidcnt
if [ $pidcnt -eq 0 ]
then
echo "start alert"
curl -d "group_name=360fenxi_jssetup&subject=datacheck-alert&content=datacheck-alert-error-plase-check" http://alarm.mis.corp.qihoo.net:8360/alarm
sleep 10s
sh start.sh
fi
