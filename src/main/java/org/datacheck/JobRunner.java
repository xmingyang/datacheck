package org.datacheck;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.Date;

public class JobRunner implements Runnable, Serializable {

    JobInfo jobinfo;
    ArrayList<JobInfo> jobqueue;
    Map<Integer, String> stautsmap;
    String project_en;
    int crontab_id;
    String scheduler_seq;
    Map<String, String> project_param;
    Map<Integer, String> runningmap;
    Map<Integer, String> pre_runningmap;
    Map<Integer, String> runningmap_aftertime;

    public JobRunner(JobInfo jobinfo, ArrayList<JobInfo> jobqueue, Map<Integer, String> stautsmap, String project_en,
            int crontab_id, String scheduler_seq, Map<String, String> project_param, Map<Integer, String> runningmap,
            Map<Integer, String> pre_runningmap, Map<Integer, String> runningmap_aftertime) {
        this.jobinfo = jobinfo;
        this.jobqueue = jobqueue;
        this.stautsmap = stautsmap;
        this.project_en = project_en;
        this.crontab_id = crontab_id;
        this.scheduler_seq = scheduler_seq;
        this.project_param = project_param;
        this.runningmap = runningmap;
        this.pre_runningmap = pre_runningmap;
        this.runningmap_aftertime = runningmap_aftertime;
    }

    public void run() {
        Logger logger = Logger.getLogger(JobRunner.class.getName());

        // opScnt("inc");
        if (!runningmap.containsKey(jobinfo.getSql_id())) {
            runningmap.put(jobinfo.getSql_id(), "");
        }
        // 时间依赖控制部分
        String after_hour = jobinfo.getHour();
        String after_min = jobinfo.getMin();
        if (CommonUtil.is_hour(after_hour) || CommonUtil.is_min(after_min)) {
            logger.info("sql_id:" + jobinfo.getSql_id() + "  start time ref" + " after_hour:" + after_hour + " after_min:" + after_min);
            int cnt = 0;
            while (true) {
                if (!runningmap_aftertime.containsKey(jobinfo.getSql_id())) {
                    runningmap_aftertime.put(jobinfo.getSql_id(), "");
                }

                Date currentdate = new Date();
                int hour = currentdate.getHours();
                int min = currentdate.getMinutes();
                // 同时设置了小时和分钟
                if (CommonUtil.is_hour(after_hour) && CommonUtil.is_min(after_min)) {
                    if (hour > Integer.parseInt(after_hour)
                            || (hour == Integer.parseInt(after_hour) && min >= Integer.parseInt(after_min))) {
                        break;
                    }

                } // 只设置分钟，大于该分钟即执行
                else if (!CommonUtil.is_hour(after_hour) && CommonUtil.is_min(after_min)) {
                    if (min >= Integer.parseInt(after_min)) {
                        break;
                    }

                } else if (CommonUtil.is_hour(after_hour) && !CommonUtil.is_min(after_min)) {
                    if (hour >= Integer.parseInt(after_hour)) {
                        break;
                    }
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                cnt++;
                // 超过6小时直接退出
                if (cnt > 1080) {
                    break;
                }

            }
            if (runningmap_aftertime.containsKey(jobinfo.getSql_id())) {
                runningmap_aftertime.remove(jobinfo.getSql_id());
            }
            logger.info("sql_id:" + jobinfo.getSql_id() + "  end time ref" + " after_hour:" + after_hour + " after_min:" + after_min);
        }

        execSql();

        if (runningmap.containsKey(jobinfo.getSql_id())) {
            runningmap.remove(jobinfo.getSql_id());
        }

    }

    public int getLog_id() {
        return log_id;
    }

    public void setLog_id(int log_id) {
        this.log_id = log_id;
    }

    private int log_id = 0;

    public void init_log() {
        PropertyConfigurator.configure("conf/log4j.properties");
        Logger logger = Logger.getLogger(JobRunner.class.getName());
        Connection con = null;
        Statement sql = null;
        ResultSet rs = null;

        try {
            con = DbCoonect.getConnectionMySql();
            if (con == null) {
                logger.error("connect is null");
                System.exit(0);
            }
            sql = con.createStatement();

            String start_date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            String datekey = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            String strSql = " insert into proj_log(project_en,proj_crontab_id,proj_scheduler_seq,sql_id,sql_cn,start_date,datekey,program_status,owner,module) values('"
                    + project_en + "'," + crontab_id + ",'" + scheduler_seq + "'," + jobinfo.getSql_id() +",'" + jobinfo.getSql_cn() + "','" + start_date + "','" + datekey + "','"
                    + "S" + "','" + jobinfo.getOwner() + "','" + jobinfo.getModule() + "')";
            sql.executeQuery("set names utf8");
            sql.executeUpdate(strSql);
            rs = sql.executeQuery("select last_insert_id()");
            if (rs.next()) {
                setLog_id(rs.getInt(1));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());

        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

    }

    public void finish_log(String status, String loginfo, int is_hasdata) {
        PropertyConfigurator.configure("conf/log4j.properties");
        Logger logger = Logger.getLogger(JobRunner.class.getName());

        Connection con = null;
        Statement sql = null;
        logger.info("sql_id:" + jobinfo.getSql_id() + " status:" + status + " finish_log db connecting ");

        try {
            con = DbCoonect.getConnectionMySql();
            if (con == null) {
                logger.error("connect is null");
              System.exit(0);
            }
            logger.info("sql_id:" + jobinfo.getSql_id() + " status:" + status + " finish_log db connected ");
            sql = con.createStatement();

            String end_date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

            int log_id = getLog_id();
            String strSql = "update proj_log set end_date='" + end_date + "',program_status='" + status + "',loginfo='"
                    + loginfo + "',is_hasdata=" + is_hasdata + " where id =" + log_id;
            logger.info("sql_id:" + jobinfo.getSql_id() + " status:" + status + " finish_log db executeUpdating ");
            sql.executeUpdate(strSql);
            logger.info("sql_id:" + jobinfo.getSql_id() + " status:" + status + " finish_log db executeUpdated ");
        } catch (Exception e) {
            // e.printStackTrace();
            logger.error(e.getMessage());

        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void execSql() {
        PropertyConfigurator.configure("conf/log4j.properties");
        Logger logger = Logger.getLogger(JobRunner.class.getName());
        logger.info("sql_id:" + jobinfo.getSql_id() + " sql_cn:" + jobinfo.getSql_cn() + " begin exec");
     //   logger.info("sql:" + jobinfo.getIp() + jobinfo.getPort() + jobinfo.getDbname() + jobinfo.getPasswd());
     //   logger.info(jobinfo.getSql_text());
        stautsmap.put(jobinfo.getSql_id(), "S");
        init_log();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String status = "";
        int is_hasdata = 0;
        try {

            con = DbCoonect.getConnection(jobinfo.getDbtype(), jobinfo.getIp(), jobinfo.getPort(), jobinfo.getDbname(), jobinfo.getUsername(), new String(CommonUtil.decode(jobinfo.getPasswd().getBytes())));
            if (con == null) {
                logger.error("connect is null "+jobinfo.getDbtype()+" "+jobinfo.getIp()+" "+jobinfo.getPort());
                //System.exit(0);
                status = "F";
                stautsmap.put(jobinfo.getSql_id(), "F");
                logger.info("sql_id: " + jobinfo.getSql_id() + " updated status F in memory");
                finish_log(status, "connect is null "+jobinfo.getDbtype()+" "+jobinfo.getIp()+" "+jobinfo.getPort(), is_hasdata);
                logger.info("sql_id: " + jobinfo.getSql_id() + " updated status F in db");
                logger.info("sql_id: " + jobinfo.getSql_id() + ": fail exec " + "connect is null "+jobinfo.getDbtype()+" "+jobinfo.getIp()+" "+jobinfo.getPort());
                CommonUtil.sendmail(
                        new StringBuilder("Hi," + jobinfo.getOwner()).append("\n").append("sql:" + jobinfo.getSql_text())
                        .append("\n").append("执行出错,请尽快修复!").append("错误信息:" + "connect is null "+jobinfo.getDbtype()+" "+jobinfo.getIp()+" "+jobinfo.getPort()).toString(),
                        "数据质量稽核SQL执行出错" + "报警[" + jobinfo.getOwner() + "]");
                return;
            }
            Date d = new Date();
            String datastr = scheduler_seq;
            stmt = con.createStatement();
            rs = stmt.executeQuery(jobinfo.getSql_text());
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            int datacnt = 0;

            // 读取数据构建邮件内容
            logger.info("begin to construct mail...");
            StringBuilder tableBuidler = new StringBuilder();
            String level = jobinfo.getLevel() == 0 ? "普通" : (jobinfo.getLevel() == 1) ? "中" : "高";

            //
            String sqlHtmlStr = jobinfo.getSql_text().replace("<", "&#60;").replace(">", "&#62;");
            tableBuidler.append(getMailCSSHead());
            tableBuidler.append("<h3>").append("任务详情").append("<br/>").append("</h3>");
            tableBuidler.append("<table border=\"1\" class=\"gridtable\">");
            tableBuidler.append("<tr>").append("<th>").append("SQL id").append("</th>").append("<td>").append(jobinfo.getSql_id()).append("</td>").append("</tr>");
            tableBuidler.append("<tr>").append("<th>").append("重要性").append("</th>").append("<td>").append(level).append("</td>").append("</tr>");
            tableBuidler.append("<tr>").append("<th>").append("模块").append("</th>").append("<td>").append(jobinfo.getModule()).append("</td>").append("</tr>");
            tableBuidler.append("<tr>").append("<th>").append("检查类型").append("</th>").append("<td>").append(jobinfo.getCheck_type()).append("</td>").append("</tr>");
            tableBuidler.append("<tr>").append("<th>").append("SQL语句").append("</th>").append("<td>").append(sqlHtmlStr).append("</td>").append("</tr>");
            tableBuidler.append("</table><br/>");
            //
            tableBuidler.append("<h3>").append("任务执行结果").append("<br/>").append("</h3>");
            tableBuidler.append("<table border=\"1\" class=\"gridtable\"><tr>");
            // 构建表头
            for (int i = 1; i <= colCount; i++) {
                tableBuidler.append("<th>");
                tableBuidler.append(rsmd.getColumnName(i));
                tableBuidler.append("</th>");
            }
            tableBuidler.append("</tr>");
            //填充内容
            while (rs.next()) {
                if (++datacnt > 10) {
                    break;
                }
                tableBuidler.append("<tr>");
                for (int i = 1; i <= colCount; i++) {
                    tableBuidler.append("<td>");
                    tableBuidler.append(rs.getString(i));
                    tableBuidler.append("</td>");
                }
                tableBuidler.append("</tr>");
            }
            tableBuidler.append("</table><br/>");
            tableBuidler.append(getMailCSSTail());
            jobinfo.setSql_text(status);
            if (datacnt > 0) {
                is_hasdata = 1;
//                logger.info("DATA IS:" + tableBuidler.toString());
                // 发邮件

                String title =  "[" + jobinfo.getOwner() + "]"+"数据稽核"+" - "+jobinfo.getModule() + " - " + jobinfo.getCheck_type() + " - " + jobinfo.getSql_cn();
                String content = new String(tableBuidler.toString().getBytes(), "utf-8");
                CommonUtil.sendmail(content, title);
                logger.info("send mail successfully, title is " + title);
            }
            status = "C";
            stautsmap.put(jobinfo.getSql_id(), "C");
            logger.info("sql_id: " + jobinfo.getSql_id() + " updated status C in memory");
            logger.info("sql_id: " + jobinfo.getSql_id() + " updating status C in db");
            finish_log(status, "", is_hasdata);
            logger.info("sql_id: " + jobinfo.getSql_id() + " updated status C in db");
            logger.info("sql_id: " + jobinfo.getSql_id() + ": success exec");

        } catch (Exception e) {
            // e.printStackTrace();
            logger.error(e.getMessage());
            status = "F";
            stautsmap.put(jobinfo.getSql_id(), "F");
            logger.info("sql_id: " + jobinfo.getSql_id() + " updated status F in memory");
            finish_log(status, e.getMessage(), is_hasdata);
            logger.info("sql_id: " + jobinfo.getSql_id() + " updated status F in db");
            logger.info("sql_id: " + jobinfo.getSql_id() + ": fail exec " + e.getMessage());
            CommonUtil.sendmail(
                    new StringBuilder("Hi," + jobinfo.getOwner()).append("\n").append("sql:" + jobinfo.getSql_text())
                    .append("\n").append("执行出错,请尽快修复!").append("错误信息:" + e.getMessage()).toString(),
                    "数据质量稽核SQL执行出错" + "报警[" + jobinfo.getOwner() + "]");

        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

    }

    public static String getMailCSSHead() {
        return "<html><head>\n"
                + "   <style type=\"text/css\">\n"
                + "table.gridtable {\n"
                + "        font-family: verdana,arial,sans-serif;\n"
                + "        font-size:11px;\n"
                + "        color:#333333;\n"
                + "        border-width: 1px;\n"
                + "        border-color: #666666;\n"
                + "        border-collapse: collapse;\n"
                + "}\n"
                + "table.gridtable th {\n"
                + "        border-width: 1px;\n"
                + "        padding: 8px;\n"
                + "        border-style: solid;\n"
                + "        border-color: #666666;\n"
                + "        background-color: #dedede;\n"
                + "}\n"
                + "table.gridtable td {\n"
                + "        border-width: 1px;\n"
                + "        padding: 8px;\n"
                + "        border-style: solid;\n"
                + "        border-color: #666666;\n"
                + "        background-color: #ffffff;\n"
                + "}\n"
                + "</style></head><body><left>";
    }

    public static String getMailCSSTail() {
        return "</left></body></html>";
    }
}
