package com.airxiechao.clusterkeeper.job.sysperf.service.impl;

import com.airxiechao.clusterkeeper.job.sysperf.entity.SystemPerf;
import com.airxiechao.clusterkeeper.job.sysperf.service.SystemPerfService;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.*;

@Service
public class SystemPerfServiceImpl implements SystemPerfService {

    private static final Logger logger = LoggerFactory.getLogger(SystemPerfServiceImpl.class);

    private static final String OS_WIN = "winnt";
    private static final String OS_LINUX = "linux";

    private OperatingSystemMXBean os = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

    @Override
    public SystemPerf getCurrentPerf() {
        double cpuLoad = os.getSystemCpuLoad();
        long memoryTotal = os.getTotalPhysicalMemorySize();
        long memoryFree = getFreeMemorySize();

        long diskTotal = 0L;
        long diskFree = 0L;
        for (FileStore store: FileSystems.getDefault().getFileStores()) {
            try {
                diskTotal += store.getTotalSpace();
                diskFree += store.getUsableSpace();
            } catch (IOException e) {

            }
        }

        SystemPerf perf = new SystemPerf();
        perf.setCpuLoad(cpuLoad);
        perf.setMemoryTotalBytes(memoryTotal);
        perf.setMemoryFreeBytes(memoryFree);
        perf.setDiskTotalBytes(diskTotal);
        perf.setDiskFreeBytes(diskFree);
        
        perf.setCreateTime(new Date());

        return perf;
    }

    /**
     * 获取可用内存bytes
     * @return
     */
    private long getFreeMemorySize(){
        String type = getOS();
        if(type.equals(OS_LINUX)){
            return getLinuxFreeMemorySize();
        }else{
            return os.getFreePhysicalMemorySize();
        }
    }

    /**
     * linux获取可用内存bytes
     * @return
     */
    private long getLinuxFreeMemorySize(){
        long mem = 0L;

        try{
            String out = executeCommand("free -b");
            String[] lines = out.split("\n");

            String[] line0 = lines[0].trim().split("\\s+");
            int i;
            for(i = 0; i < line0.length; ++i){
                if(line0[i].equals("available")){
                    break;
                }
            }

            String[] line1 = lines[1].trim().split("\\s+");
            mem = Long.parseLong(line1[i+1]);
        }catch (Exception e){
            logger.error("获取linux系统可用内存发生错误", e);
        }

        return mem;
    }

    /**
     * 执行命令行
     * @param command
     * @return
     */
    private static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {

        }

        return output.toString();
    }

    /**
     * 获取操作系统类型
     * @return
     */
    private static String getOS(){
        String os = System.getProperty("os.name").toLowerCase();
        if(os.startsWith("win")){
            return OS_WIN;
        }else{
            return OS_LINUX;
        }
    }
}
