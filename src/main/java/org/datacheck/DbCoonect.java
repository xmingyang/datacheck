package org.datacheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbCoonect {
	public static Connection getConnectionMySql() throws Exception {
		Connection c = null;
		try {
			Class.forName(PropHelper.getStringValue("jdbc.driverClassName"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			
			c = DriverManager
					.getConnection(
							PropHelper.getStringValue("jdbc.url"),
							PropHelper.getStringValue("jdbc.username"), PropHelper.getStringValue("jdbc.password"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return c;
	}
	//jdbc:mysql://localhost:3306/jobstreamdb?autoReconnect=true
	public static Connection getConnection(String dbtype,String ip,int port,String dbname,String username,String passwd) throws Exception {
		Connection c = null;
		String jdbc_driverClassName="";
		String jdbc_url="";
		if (dbtype.equals("mysql"))
		{
			jdbc_driverClassName="com.mysql.jdbc.Driver";
			jdbc_url="jdbc:mysql://"+ip+":"+String.valueOf(port)+"/"+dbname;
			
		}
		else if (dbtype.equals("greenplum"))
		{
			jdbc_driverClassName="org.postgresql.Driver";
			jdbc_url="jdbc:postgresql://"+ip+":"+String.valueOf(port)+"/"+dbname;
			
		}
		else 
			{
			return c;
			}
		try {
			Class.forName(jdbc_driverClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			c = DriverManager
					.getConnection(
							jdbc_url,
							username, passwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return c;
	}

}
