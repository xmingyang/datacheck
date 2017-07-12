package org.datacheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {

    public static void sendmail(String msg, String title) {
        PropertyConfigurator.configure("conf/log4j.properties");
        Logger logger = Logger.getLogger(CommonUtil.class.getName());
        logger.info("调用本地shell发邮件，邮件内容为:" + msg);
        //String newmsg=msg.replace(" ", "");

        try {
            String cmd = PropHelper.getStringValue("cmd");
            String emaillist = PropHelper.getStringValue("email");
			//	logger.info("email command:"+cmd+" \'"+newmsg+"\' "+title+" "+emaillist);

            //直接调用python脚本
//            String[] cmds = new String[5];
//            cmds[0] = cmd;
//            cmds[1] = "bin/mail.py";
//            cmds[2] = "bin/mail_job.cfg";
//            cmds[3] = title;
//            cmds[4] = msg;
            //调用alert.sh
            String[] cmds = new String[4];
            cmds[0] = cmd;
            cmds[1] = msg;
            cmds[2] = title;
            cmds[3] = emaillist;

            //   Process process= Runtime.getRuntime().exec(cmd+" \""+newmsg+"\" "+title+" "+emaillist); 
            Process process = Runtime.getRuntime().exec(cmds);
            int exitValue = process.waitFor();

            if (0 != exitValue) {
                logger.error("邮件发送失败. error code is :" + exitValue);
            } else {
                logger.info("邮件发送成功.emailist:" + emaillist + " title:" + title);
            }
        } catch (Exception e) {
            logger.error("邮件发送失败. " + e);
        }
    }

    public static String expr_date(String expr_date) {
        Pattern pat = Pattern.compile("expr_date\\(([date|hour].*),([a-zA-z0-9-/ ]+)\\)");
        Matcher mat = pat.matcher(expr_date);
        if (mat.find()) {
            String dateval = mat.group(1);
            String dateformat = mat.group(2);

            Calendar thiscal = Calendar.getInstance();

            Date thisb = new Date();
            thiscal.setTime(thisb);

            // System.out.println(sdf.format(thiscal.getTime()));
            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);

            if (dateval.contains("-")) {
                String[] datevals = dateval.split("-");

                if (datevals[0].trim().equals("date")) {
                    thiscal.add(Calendar.DAY_OF_MONTH, Integer.parseInt("-" + datevals[1].trim()));

                    return sdf.format(thiscal.getTime());
                } else if (datevals[0].trim().equals("hour")) {

                    thiscal.add(Calendar.HOUR_OF_DAY, Integer.parseInt("-" + datevals[1].trim()));
                    return sdf.format(thiscal.getTime());

                }
            } else if (dateval.contains("+")) {
                String[] datevals = dateval.split("+");

                if (datevals[0].trim().equals("date")) {
                    thiscal.add(Calendar.DAY_OF_MONTH, Integer.parseInt("+" + datevals[1].trim()));

                    return sdf.format(thiscal.getTime());
                } else if (datevals[0].trim().equals("hour")) {
                    thiscal.add(Calendar.HOUR_OF_DAY, Integer.parseInt("+" + datevals[1].trim()));
                    return sdf.format(thiscal.getTime());

                }
            } else {

                return sdf.format(thiscal.getTime());
                /*
                 if (dateval.trim().equals("date"))
                 {
                 //thiscal.add(Calendar.DAY_OF_MONTH,Integer.parseInt("+"+dateval.trim()));
					
                 return sdf.format(thiscal.getTime());
                 }
                 else if (dateval.trim().equals("hour"))
                 {
                 //thiscal.add(Calendar.HOUR_OF_DAY,Integer.parseInt("+"+dateval.trim()));
                 return sdf.format(thiscal.getTime());
					
                 }
                 */
            }

        }
        return expr_date;
    }

    //验证是否有效的小时
    public static boolean is_hour(String hour) {
        if (hour != null && !hour.equals("")) {
            if (is2Numberic(hour)) {
                if (Integer.parseInt(hour) >= 0 && Integer.parseInt(hour) <= 23) {
                    return true;
                }
            }
        }
        return false;
    }

    //验证是否有效的分钟
    public static boolean is_min(String min) {
        if (min != null && !min.equals("")) {
            if (is2Numberic(min)) {
                if (Integer.parseInt(min) >= 0 && Integer.parseInt(min) <= 59) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean is2Numberic(String str) {
        Pattern pattern = Pattern.compile("[0-9]{1,2}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * @param bytes
     * @return
     */
    public static byte[] decode(final byte[] bytes) {
        return Base64.decodeBase64(bytes);
    }

    /**
     * 二进制数据编码为BASE64字符串
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(final byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            System.out.println("args[0]:" + args[0]);
            System.out.println("args[1]:" + args[1]);
            sendmail(args[0], args[1]);
        }
    }

}
