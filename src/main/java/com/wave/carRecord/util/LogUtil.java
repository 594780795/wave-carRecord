package com.wave.carRecord.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    public static final Logger log = LoggerFactory.getLogger(LogUtil.class);
    
    public static void main(String[] args) {
        try {
            LogUtil.errorMethod();
        } catch (Exception e) {
            log.error("localizaizedMessage : {}", e.getLocalizedMessage());
            log.error("exception message : {}", e.getMessage());
            log.error("exception cause : {}", e.getCause());
            log.error("exception suppressed : {}", e.getSuppressed());
            log.error("exception getStackTrace : {}", JSON.toJSONString(e.getStackTrace()));
            //异常输出
            log.error("exception toString and track space : [{}]", e);
            log.error("---------------------------------------------");
            log.error(LogUtil.errorTrackSpace(e));
            log.error("---------------------------------------------");
            e.printStackTrace();
        }
    }
    
    /**
     * 制造异常的方法
     */
    private static void errorMethod() {
        String str = null;
        String[] a = new String[]{"1"};
        System.out.println(a[2]);
        System.out.println(str.toString());
    }
    
    /**
     * 输出异常信息
     * @param e
     * @return
     */
    private static String errorTrackSpace(Exception e) {
        StringBuffer sb = new StringBuffer();
        if (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append("\r\n\t").append(element);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
