#!/bin/sh
basepath=$(cd `dirname $0`; pwd)
if [ $# -eq 0 ] 
then
  echo "please set parameter:filename"
  exit 1
fi
filename=$1
host=x.x.x.x
user=x
passwd=x
db=datacheck
port=3306
#sed  '1d' $filename >$filename.temp
#iconv -f gbk -t utf-8 $filename.temp >$filename.new
python covert_xls.py $filename $filename.tmp
sed  '1d' $filename.tmp >$filename.new
dos2unix ./$filename.new
echo `date` "truncate import"
mysql -h $host -u$user  -p$passwd -P $port -D $db <<EOF
truncate table proj_sql_import;
EOF
if [ $? -ne 0 ] ; then
  echo "清空表proj_sql_import出错"
  exit 1
fi
  
echo `date` "load import"
#character set utf8
mysql -h $host -u$user  -p$passwd -P $port -D $db -e "LOAD DATA LOCAL INFILE '$filename.new' INTO table proj_sql_import character set utf8 FIELDS TERMINATED BY ','  ENCLOSED BY '\"' LINES TERMINATED BY '\n'  (project_id,sql_text,sql_cn,level,priority,owner,datasource_id,module,check_type,after_hour,after_min);"
if [ $? -ne 0 ] ; then
  echo "load表proj_sql_import出错!"
  exit 1
fi
mysql -h $host -u$user  -p$passwd -P $port -D $db <<EOF
truncate table proj_sql_bak;
insert into proj_sql_bak select * from proj_sql;
EOF
if [ $? -ne 0 ] ; then
  echo "备份出错!"
  exit 1
fi

mysql -h $host -u$user  -p$passwd -P $port -D $db <<EOF
truncate table proj_sql;
insert into proj_sql select * from proj_sql_import;
EOF
if [ $? -ne 0 ] ; then
  echo "正式导入出错!"
  exit 1
fi
echo "完成导入!"
