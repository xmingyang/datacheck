#!/bin/sh
#project_en=fenxi_main_day|fenxi_main_hour
#指定项目手工跑批
#usage 1: sh manual_job.sh project_en=xx
#指定项目手工跑批，并传参
#usage 1: sh manual_job.sh project_en=xx '${cdate}'=20160804
#usage 1: sh manual_job.sh project_en=xx '${chour}'=2016080400

basepath=$(cd `dirname $0`; pwd)
cd $basepath/../
java -cp datacheck-0.0.1-SNAPSHOT.jar org.datacheck.DataCheck $*
