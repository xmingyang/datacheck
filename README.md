JobStream
===================================
ETL workflow schedule system


Requires
--------
Java version>=1.6x   
Maven 3.x   
mysql 5.x   

Introduce
--------
1、support various job scheduled,includes shell、python、java、mapreduce   
2、build dependencies stream automatically according to the input and output       
3、Smart breakpoints to run again   
4、Job Priority control   
5、The number of concurrent control    
6、Error retry mechanism   
7、Job Error alert at once   

Installation
--------
```
$git clone https://github.com/xmingyang/JobStream.git   
$cd JobStream    
$mvn package -Pdist,native -DskipTests –Dtar   
$cd target   
$tar -zxvf jobStream-0.0.1-SNAPSHOT.tar.gz   
$cp ../quartz.properties jobStream-0.0.1-SNAPSHOT/    
$cd jobStream-0.0.1-SNAPSHOT   
$cp jobStream-0.0.1-SNAPSHOT/lib/jobStream-0.0.1-SNAPSHOT.jar ../
```   
You can move jobStream-0.0.1-SNAPSHOT dir to your setup path,and set $JOBSTREAM_HOME environment variable         
Prepare a mysql db,create jobstreamdb database and initialize jobstream table by ddl/mysql_table.sql    
modify conf/config.properties jdbc.url jdbc.username jdbc.password     
modify quartz.properties org.quartz.dataSource.myDS.URL org.quartz.dataSource.myDS.user org.quartz.dataSource.myDS.password    
 
**start jobstream service:**
```    
cd $JOBSTREAM_HOME/bin    
sh start.sh   
```


**stop jobstream service:**
```   
cd $JOBSTREAM_HOME/bin   
sh stop.sh
```   

Example:
--------
```
insert into project (project_en,project_cn,max,param) values('proj_test','proj_test',2,'${hour}=expr_date(hour-2,HH);${a}=aaaaaaa');    
insert into proj_jobdetail(project_id,job_en,job_cn,priority,ip,port,user,path,hdfs_input,hdfs_output,job_type_id,param) 
    values(1,'test30_1','test30_1',0,'192.168.1.1',22,'root','/root/test30_1.sh','','/user/test30_1',1,'${a};${hour}');    
insert into proj_jobdetail(project_id,job_en,job_cn,priority,ip,port,user,path,hdfs_input,hdfs_output,job_type_id) 
    values(1,'test30_2','test30_2',0,'192.168.1.1',22,'root','/root/test30_2.sh','','/user/test30_2',1);    
insert into proj_jobdetail(project_id,job_en,job_cn,priority,ip,port,user,path,hdfs_input,hdfs_output,job_type_id) 
    values(1,'test30_3','test30_3',0,'192.168.1.1',22,'root','/root/test30_3.sh','/user/test30_1;/user/test30_2','/user/test30_3',1);        
insert into proj_crontab(project_id,cronexpression,is_enable) values(1,'0 30 3 * * ?',1);
```          

So do it,we add a project named "proj_test" ,the project run max 2 jobs and the project contain two parameter:    
${hour}=expr_date(hour-2,HH) mean if current hour is 10,${hour} return 8    
${a}=aaaaaaa mean parameter ${a} is constant value is aaaaaaa    

In this project,we add three jobs     test30_1（/root/test30_1.sh）、test30_2（/root/test30_2.sh）、test30_3（/root/test30_3.sh),according to the hdfs_input and hdfs_output,    
build dependencies stream automatically ,run jobs(test30_1,test30_2) successed ,then run test30_3    

Next Step
--------
Develop UI to manage all    

**E-Mail:**louiscool@126.com
