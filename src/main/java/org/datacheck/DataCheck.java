package org.datacheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DataCheck {
	// 项目代码，项目标识
	public String getProject_en() {
		return project_en;
	}

	public void setProject_en(String project_en) {
		this.project_en = project_en;
	}

	// crontab id，只有被自动调度的时候才有该id,写日志带入，手工跑该类id=0
	public int getCrontab_id() {
		return crontab_id;
	}

	public void setCrontab_id(int crontab_id) {
		this.crontab_id = crontab_id;
	}

	// 整个项目执行统一的序号，基于当前时间生成，作为版本号使用
	public String getScheduler_seq() {
		return scheduler_seq;
	}

	public void setScheduler_seq(String scheduler_seq) {
		this.scheduler_seq = scheduler_seq;
	}

	public String getCrontab_param() {
		return crontab_param;
	}

	public void setCrontab_param(String crontab_param) {
		this.crontab_param = crontab_param;
	}

	private String project_en;
	private int crontab_id = 0;
	private String scheduler_seq;
	// 调度表设置的参数
	private String crontab_param = "";
	private int max = 2; // 并发数的控制，默认值2

	// 手工传入的参数
	private HashMap<String, String> proj_param_manual = new HashMap<String, String>();

	public HashMap<String, String> getProj_param_manual() {
		return proj_param_manual;
	}

	public void setProj_param_manual(HashMap<String, String> proj_param_manual) {
		this.proj_param_manual = proj_param_manual;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	ArrayList<JobInfo> jobqueue;
	Map<Integer, String> stautsmap;// job运行状态
	Map<String, String> project_param;
	Map<Integer, String> runningmap;
	Map<Integer, String> pre_runningmap;
	Map<Integer, String> runningmap_aftertime;

	public DataCheck(ArrayList<JobInfo> jobqueue, Map<Integer, String> stautsmap, Map<String, String> project_param,
			Map<Integer, String> runningmap, Map<Integer, String> pre_runningmap,
			Map<Integer, String> runningmap_aftertime) {
		this.jobqueue = jobqueue;
		this.stautsmap = stautsmap;
		this.project_param = project_param;
		this.runningmap = runningmap;
		this.pre_runningmap = pre_runningmap;
		this.runningmap_aftertime = runningmap_aftertime;
	}

	public void init() {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger logger = Logger.getLogger(DataCheck.class.getName());
		logger.info("init start!");
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = DbCoonect.getConnectionMySql();
			if (con == null) {
				logger.error("connect is null");
				System.exit(0);
			}
			stmt = con.createStatement();
			String strSql = "select trim(project_en),trim(project_cn),a.id as sql_id,trim(sql_text),trim(sql_cn),priority,level,trim(a.owner),after_hour,after_min,a.datasource_id,c.dbtype,c.ip,c.port,c.dbname,c.username,c.passwd,a.module,a.check_type from proj_sql a,project b,datasource c where "
					+ " a.project_id=b.id and a.datasource_id=c.id and trim(b.project_en)= '" + project_en + "'";
			String projectSql = "select max from project where trim(project_en)='" + project_en + "'";
			rs = stmt.executeQuery(projectSql);
			// 项目级别参数
			if (rs.next()) {
				// 项目并发个数
				int maxval = rs.getInt(1);
				if (maxval > 0) {
					setMax(maxval);
					logger.info("setMax:"+maxval);
				}
			}
			stmt.executeQuery("set names utf8");
			rs = stmt.executeQuery(strSql);
			while (rs.next()) {
				JobInfo jobinfo = new JobInfo();
				jobinfo.setSql_id(rs.getInt(3));
				jobinfo.setSql_text(rs.getString(4));
				jobinfo.setSql_cn(rs.getString(5));
				jobinfo.setPriority(rs.getInt(6));
				jobinfo.setLevel(rs.getInt(7));
				jobinfo.setOwner(rs.getString(8));
				jobinfo.setHour(rs.getString(9));
				jobinfo.setMin(rs.getString(10));
				jobinfo.setDatasource_id(rs.getInt(11));
				jobinfo.setDbtype(rs.getString(12));
				jobinfo.setIp(rs.getString(13));
				jobinfo.setPort(rs.getInt(14));
				jobinfo.setDbname(rs.getString(15));
				jobinfo.setUsername(rs.getString(16));
				jobinfo.setPasswd(rs.getString(17));
				jobinfo.setModule(rs.getString(18));
				jobinfo.setCheck_type(rs.getString(19));
				long starttime = new Date().getTime();
				jobinfo.setStarttime(starttime);
				jobqueue.add(jobinfo);
				pre_runningmap.put(jobinfo.getSql_id(), "");
			}
			logger.info("init jobqueue size:" + jobqueue.size());
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(e.getMessage());

		} finally {
			try {
				rs.close();
				stmt.close();
				con.close();
			} catch (SQLException e) {
				// e.printStackTrace();
				logger.error(e.getMessage());
			}
		}
		resortJobqueue();
	}

	public void resortJobqueue() {
		Comparator<JobInfo> comp = new Comparator<JobInfo>() {
			public int compare(JobInfo o1, JobInfo o2) {
				int res = o1.getPriority() - o2.getPriority();
				if (res == 0) {
					if (o1.getStarttime() < o2.getStarttime())
						res = 1;
					else
						res = (o1.getStarttime() == o2.getStarttime() ? 0 : -1);
				}
				return -res;
			}
		};
		synchronized (jobqueue) {
			Collections.sort(jobqueue, comp);
		}
	}

	public void start() {
		PropertyConfigurator.configure("conf/log4j.properties");
		Logger logger = Logger.getLogger(DataCheck.class.getName());
		int x = 0;
		int c = 0; // 状态为s的个数
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(getMax(), 20, 5, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardOldestPolicy());
		while (true) {
			if (jobqueue.isEmpty()) {
				if (runningmap.size() == 0) {
					x++;
					logger.info("project_en:" + this.getProject_en() + " crontab_id:" + this.getCrontab_id()
							+ " jobqueue is empty and  running jobs:0:seq:" + x);
				}
			}

			c = runningmap.size() - runningmap_aftertime.size();
			if (c < getMax()) {
				JobInfo jobinfo = null;
				if (!jobqueue.isEmpty()) {
					jobinfo = jobqueue.remove(0);
				}
				if (jobinfo != null) {
					pre_runningmap.remove(jobinfo.getSql_id());
					x = 0;
					threadPool.execute(new JobRunner(jobinfo, jobqueue, stautsmap, project_en, crontab_id,
							scheduler_seq, project_param, runningmap, pre_runningmap, runningmap_aftertime));
					// JobRunner jr = new JobRunner(jobinfo, jobqueue,
					// stautsmap, project_en, crontab_id, scheduler_seq,
					// project_param, runningmap, pre_runningmap,
					// runningmap_aftertime);
					// jr.start();
					try {
						Thread.sleep(600);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				x = 0;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (x > 5) {
				threadPool.shutdown();
				logger.info("project_en:" + this.getProject_en() + " crontab_id:" + this.getCrontab_id()
						+ " the whole JobStream  job exit!");
				break;
			}

		}
	}

	public static void main(String[] args) {
		/*
		 * 传参： 必需： project_en
		 * 
		 * 
		 * 
		 */
		if (args.length < 1) {
			System.out.println("必须传入参数");
			System.exit(1);
		}
		HashMap<String, String> param_value = new HashMap<String, String>();
		// 手工指定项目内参数
		HashMap<String, String> proj_param_manual = new HashMap<String, String>();
		for (String para : args) {
			if (!para.contains("=")) {
				System.out.println("传入参数格式:param_name=param_value");
				System.exit(1);
			}
			String[] s = para.split("=");
			// param_value.put(s[0], s[1]);

			System.out.println("传入参数:" + para);
			if (para.startsWith("$")) {
				System.out.println("传入项目内部参数" + s[0] + ":" + s[1]);
				proj_param_manual.put(s[0], s[1]);
			} else {
				param_value.put(s[0], s[1]);
			}

		}
		if (!param_value.containsKey("project_en")) {
			System.out.println("必须传入参数:project_en");
			System.exit(1);
		}
		ArrayList<JobInfo> jobqueue = new ArrayList<JobInfo>();
		Map<Integer, String> stautsmap = new ConcurrentHashMap<Integer, String>();// job运行状态
		Map<String, String> project_param = new HashMap<String, String>();
		// 正运行的作业map列表
		Map<Integer, String> runningmap = new ConcurrentHashMap<Integer, String>();
		// 准备运行，加入队列的作业map列表
		Map<Integer, String> pre_runningmap = new HashMap<Integer, String>();
		// 设置了after time的作业，正运行还未到达after time时间的作业map列表
		Map<Integer, String> runningmap_aftertime = new ConcurrentHashMap<Integer, String>();
		DataCheck jobmain = new DataCheck(jobqueue, stautsmap, project_param, runningmap, pre_runningmap,
				runningmap_aftertime);
		jobmain.setProject_en(param_value.get("project_en"));
		jobmain.setScheduler_seq(new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()));
		jobmain.setProj_param_manual(proj_param_manual);
		jobmain.init();
		jobmain.start();
	}

}
