package com.airxiechao.clusterkeeper.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 命令行辅助类
 */
public class CommandLineUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineUtil.class);

    public static String executeCommand(String command){
        return executeCommand(command, null);
    }

    public static String executeCommand(String[] command){
        return executeCommand(null, command);
    }

    private static String executeCommand(String command, String[] arrCommand){
        StringBuffer outBuf = new StringBuffer();
        StringBuffer errBuf = new StringBuffer();

        Process p = null;
        try {
            if(null != command){
                p = Runtime.getRuntime().exec(command);
            }else{
                p = Runtime.getRuntime().exec(arrCommand);
            }

            p.waitFor();

            // 获取stderr
            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String err;
            while ((err = errReader.readLine())!= null) {
                errBuf.append(err + "\n");
            }

            // 获取stdout
            BufferedReader outReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out;
            while ((out = outReader.readLine())!= null) {
                outBuf.append(out + "\n");
            }

        } catch (Exception e) {
            logger.error("执行命令错误：{}", (null!=command?command:StringUtils.join(arrCommand, " ")), e);
            throw new RuntimeException(e.getMessage());
        }

        if(!errBuf.toString().isEmpty()){
            RuntimeException e = new RuntimeException(errBuf.toString().trim());
            logger.error("执行命令错误：{}", (null!=command?command:StringUtils.join(arrCommand, " ")), e);
            throw e;
        }

        return outBuf.toString().trim();
    }
}
