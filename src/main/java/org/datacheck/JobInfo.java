package org.datacheck;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

public class JobInfo {
	
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public long getStarttime() {
		return starttime;
	}
	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}
	public String getSql_text() {
		return sql_text;
	}
	public void setSql_text(String sql_text) {
		this.sql_text = sql_text;
	}
	public String getSql_cn() {
		return sql_cn;
	}
	public void setSql_cn(String sql_cn) {
		this.sql_cn = sql_cn;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getMin() {
		return min;
	}
	public void setMin(String min) {
		this.min = min;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getSql_id() {
		return sql_id;
	}
	public void setSql_id(int sql_id) {
		this.sql_id = sql_id;
	}
	public int getDatasource_id() {
		return datasource_id;
	}
	public void setDatasource_id(int datasource_id) {
		this.datasource_id = datasource_id;
	}
	public String getDbtype() {
		return dbtype;
	}
	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getCheck_type() {
		return check_type;
	}
	public void setCheck_type(String check_type) {
		this.check_type = check_type;
	}
	private int priority=0;
	private long starttime;
	private String sql_text;
	private String sql_cn;
	private String owner;
	private String hour;
	private String min;
	private int level=0;
	private int sql_id=0;
	private int datasource_id=0;
	private String dbtype="";
	private String ip="";
	private int port=0 ;
	private String dbname="";
	private String	username="";
	private String passwd="";
	private String module="";
	private String check_type="";
	
	

}
