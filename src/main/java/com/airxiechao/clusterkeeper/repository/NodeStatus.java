package com.airxiechao.clusterkeeper.repository;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.airxiechao.clusterkeeper.job.sysperf.entity.SystemPerf;
import com.airxiechao.clusterkeeper.util.DateTimeDeserializer;
import com.airxiechao.clusterkeeper.util.DateTimeSerializer;

import java.util.Date;
import java.util.List;

/**
 * 节点信息包装
 */
public class NodeStatus {

    private String nodeId = "";
    private boolean isMaster = false;
    private boolean isAvailable = false;
    private RunningStatus runningStatus = RunningStatus.OFFLINE;
    private SystemPerf systemPerf;
    private Date lastStartMasterTime;
    private String[] notAvailableEvents;
    private List<String> nodeList;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(RunningStatus runningStatus) {
        this.runningStatus = runningStatus;
    }

    public SystemPerf getSystemPerf() {
        return systemPerf;
    }

    public void setSystemPerf(SystemPerf systemPerf) {
        this.systemPerf = systemPerf;
    }

    @JsonSerialize(using = DateTimeSerializer.class)
    public Date getLastStartMasterTime() {
        return lastStartMasterTime;
    }

    @JsonDeserialize(using = DateTimeDeserializer.class)
    public void setLastStartMasterTime(Date lastStartMasterTime) {
        this.lastStartMasterTime = lastStartMasterTime;
    }

    public String[] getNotAvailableEvents() {
        return notAvailableEvents;
    }

    public void setNotAvailableEvents(String[] notAvailableEvents) {
        this.notAvailableEvents = notAvailableEvents;
    }

    public List<String> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<String> nodeList) {
        this.nodeList = nodeList;
    }
}

