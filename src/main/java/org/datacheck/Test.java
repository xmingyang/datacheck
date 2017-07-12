package org.datacheck;

import java.sql.Connection;
import java.util.HashMap;
import org.datacheck.CommonUtil;

public class Test {
	public static void main(String[] args) throws Exception
	{
		String ip="10.160.133.79";
		int port=5432;
		String db="adfenxi";
		String username="adfenxi";
		String passwd="MjFmZGNlYmQ3MTU2ZDFiOQ==";
		//DbCoonect.getConnection(jobinfo.getDbtype(), jobinfo.getIp(), jobinfo.getPort(), jobinfo.getDbname(), jobinfo.getUsername(), new String(CommonUtil.decode(jobinfo.getPasswd().getBytes())));
		Connection con = DbCoonect.getConnection("greenplum", ip, port, db,username, new String(CommonUtil.decode(passwd.getBytes())));
		if (con == null) {
			System.out.println("x");
			
		}
		else
			System.out.println("y");

	}

}
