datacheck
===================================
Data quality check tools by execute sql


Requires
--------
Java version>=1.6x   
Maven 3.x   
mysql 5.x   

Introduce
--------
1、support data sources mysql and greenplum   
2、config sql to execute,if result product data,alert       
4、sql Priority control   
5、The number of concurrent control    

Installation
--------
```
$git clone https://github.com/xmingyang/datacheck.git   
$cd datacheck
$mvn package -Pdist,native -DskipTests –Dtar   
$cd target   
$tar -zxvf datacheck-0.0.1-SNAPSHOT.tar.gz   
$cp ../quartz.properties jobStream-0.0.1-SNAPSHOT/    
$cd  datacheck-0.0.1-SNAPSHOT   
$cp datacheck-0.0.1-SNAPSHOT/lib/datacheck-0.0.1-SNAPSHOT.jar ../
```   
You can move datacheck-0.0.1-SNAPSHOT dir to your setup path,and set $DATACHECK_HOME environment variable         
Prepare a mysql db,create datacheck database and initialize datacheck table by ddl/datacheck.sql    
modify conf/config.properties jdbc.url jdbc.username jdbc.password     
modify quartz.properties org.quartz.dataSource.myDS.URL org.quartz.dataSource.myDS.user org.quartz.dataSource.myDS.password    
 
**start datacheck service:**
```    
cd $DATACHECK_HOME/bin    
sh start.sh   
```


**stop datacheck service:**
```   
cd $DATACHECK_HOME/bin   
sh stop.sh
```   

Example:
--------
```
add a project:
insert into project (project_en,project_cn,max) values('proj_test','proj_test',2);    
add your datasoure:

config your sql and import mysql:
download import/check.xlsx module file to your pc,then write your sql and datasource ..
setup xlrd to convert xls to csv then load to mysql
pip install xlrd
cd import
sh mysql_import.sh check.xlsx

config schedule time:
insert into proj_crontab(project_id,cronexpression,is_enable) values(1,'0 30 3 * * ?',1);
```          

So do it,we add a project named "proj_test" ,the project run max 2 jobs and the project contain two parameter:    

**E-Mail:**louiscool@126.com
