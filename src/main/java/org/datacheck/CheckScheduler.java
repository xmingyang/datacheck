
package org.datacheck;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class CheckScheduler {
	public static void scheduler() {
		/*
		 * throws SchedulerException
		 */
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger logger = Logger.getLogger(CheckScheduler.class.getName());
		logger.info("scheduler init..");
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = null;

		try {
			sched = sf.getScheduler();
			sched.clear();
			sched.start();
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Connection con = null;
		Statement sql = null;
		ResultSet rs = null;
		JobDetail job = null;
		CronTrigger trigger = null;
		HashMap<String, String> enable_crontab = new HashMap<String, String>();
		HashMap<String, String> enable_crontab0 = null;

		while (true) {
			enable_crontab0 = new HashMap<String, String>();
			try {

				con = DbCoonect.getConnectionMySql();
				if (con == null) {

					System.exit(0);
				}

				sql = con.createStatement();

				String strSql = "select a.id,b.project_en,a.cronexpression from proj_crontab a,project b where"
						+ " a.project_id=b.id and a.is_enable=1 ";
				// System.out.println("3333333333:" + strSql);
				rs = sql.executeQuery(strSql);
				while (rs.next()) {
					int crontab_id = rs.getInt(1);
					String project_en = rs.getString(2);
					String cronexpression = rs.getString(3);
					// int max=rs.getInt(5);
					/*
					 * if (sched.getJobDetail(new
					 * JobKey(project_en+String.valueOf(crontab_id)),project_en+
					 * String.valueOf(crontab_id))==null)) {
					 * 
					 * }
					 */
					String jobid = project_en + ";" + String.valueOf(crontab_id);
					// String jobid=String.valueOf(crontab_id);
					JobDetail job1 = sched.getJobDetail(new JobKey(jobid, jobid));
					enable_crontab0.put(jobid, cronexpression);
					// 新加入的scheduler
					if (job1 == null) {
						// System.out.println("scheduler job:"+jobid+"
						// cronexpression:"+cronexpression+" max:"+max);
						logger.info("scheduler add project:" + jobid + " cronexpression:" + cronexpression);
						job = newJob(SchedJobExec.class).withIdentity(jobid, jobid).build();
						trigger = newTrigger().withIdentity(jobid, jobid).withSchedule(cronSchedule(cronexpression))
								.build();
						// System.out.println(trigger.getCronExpression());
						job.getJobDataMap().put("project_en", project_en);
						job.getJobDataMap().put("crontab_id", crontab_id);
						// job.getJobDataMap().put("max", max);

						sched.scheduleJob(job, trigger);

					}

				}
				enable_crontab = enable_crontab0;
			} catch (Exception e) {
				// e.printStackTrace();
				logger.error(e.getMessage());

			} finally {
				try {
					sql.close();
					con.close();
				} catch (SQLException e) {
					// e.printStackTrace();
					logger.error(e.getMessage());
				}
			}
			// 删除过期的crontab,或者修改变更过crontabexpression max的
			try {
				for (String groupName : sched.getJobGroupNames()) {
					for (JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
						String jobName = jobKey.getName();
						String jobGroup = jobKey.getGroup();
						List<Trigger> triggers = (List<Trigger>) sched.getTriggersOfJob(jobKey);
						// Date nextFireTime =
						// triggers.get(0).getNextFireTime();
						// System.out.println("[jobName] : " + jobName + "
						// [groupName] : " + jobGroup + " - " + nextFireTime);
						CronTrigger trig = (CronTrigger) triggers.get(0);
						// 获取当前正在调度的表达式
						String cronexpression = trig.getCronExpression();
						// 获取当前正在调度的并发数
						// int
						// max=sched.getJobDetail(jobKey).getJobDataMap().getInt("max");

						if (!enable_crontab.containsKey(jobName)) {
							// System.out.println("delete job scheduler");
							logger.info("scheduler delete project:" + jobName + " cronexpression:" + cronexpression);
							// removes the given trigger
							sched.unscheduleJob(new TriggerKey(jobName, jobGroup));
							// removes all triggers to the given job
							sched.deleteJob(new JobKey(jobName, jobGroup));
						} else {
							String enable_cronexpression = enable_crontab.get(jobName);
							// int
							// enable_max=Integer.parseInt(enable_crontab.get(jobName).split(";")[1]);
							if (!enable_cronexpression.equals(cronexpression)) {// 删除当前调度，重建

								// System.out.println("update job scheduler");
								logger.info("scheduler update project:" + jobName + " cronexpression:"
										+ enable_cronexpression);
								// removes the given trigger
								sched.unscheduleJob(new TriggerKey(jobName, jobGroup));
								// removes all triggers to the given job
								sched.deleteJob(new JobKey(jobName, jobGroup));
								job = newJob(SchedJobExec.class).withIdentity(jobName, jobName).build();
								trigger = newTrigger().withIdentity(jobName, jobName)
										.withSchedule(cronSchedule(enable_cronexpression)).build();

								// System.out.println(trigger.getCronExpression());
								job.getJobDataMap().put("project_en", jobName.split(";")[0]);
								job.getJobDataMap().put("crontab_id", jobName.split(";")[1]);
								// job.getJobDataMap().put("max", enable_max);

								sched.scheduleJob(job, trigger);

							}
						}
					}
				}
			} catch (SchedulerException exs) {
				// exs.printStackTrace();
				logger.error(exs.getMessage());
			}

			try {
				// Thread.sleep(200000);
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				logger.error(e.getMessage());
			}

		}

	}

	public static void main(String[] args) throws SchedulerException {

		scheduler();

	}

}
