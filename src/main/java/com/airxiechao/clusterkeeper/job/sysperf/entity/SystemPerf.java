package com.airxiechao.clusterkeeper.job.sysperf.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.airxiechao.clusterkeeper.util.DateTimeDeserializer;
import com.airxiechao.clusterkeeper.util.DateTimeSerializer;

import java.util.Date;

public class SystemPerf {

    private Double cpuLoad;
    private Long memoryTotalBytes;
    private Long memoryFreeBytes;
    private Long diskTotalBytes;
    private Long diskFreeBytes;
    private Date createTime;

    public Double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public Long getMemoryTotalBytes() {
        return memoryTotalBytes;
    }

    public void setMemoryTotalBytes(Long memoryTotalBytes) {
        this.memoryTotalBytes = memoryTotalBytes;
    }

    public Long getMemoryFreeBytes() {
        return memoryFreeBytes;
    }

    public void setMemoryFreeBytes(Long memoryFreeBytes) {
        this.memoryFreeBytes = memoryFreeBytes;
    }

    public Long getDiskTotalBytes() {
        return diskTotalBytes;
    }

    public void setDiskTotalBytes(Long diskTotalBytes) {
        this.diskTotalBytes = diskTotalBytes;
    }

    public Long getDiskFreeBytes() {
        return diskFreeBytes;
    }

    public void setDiskFreeBytes(Long diskFreeBytes) {
        this.diskFreeBytes = diskFreeBytes;
    }

    @JsonSerialize(using = DateTimeSerializer.class)
    public Date getCreateTime() {
        return createTime;
    }

    @JsonDeserialize(using = DateTimeDeserializer.class)
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
