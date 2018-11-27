package com.airxiechao.clusterkeeper.repository;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 节点数据存储单例
 */
@Component
public class DataStore {

    /**
     * 节点ID
     */
    private String nodeId = UUID.randomUUID().toString();

    /**
     * 是否是主节点
     */
    private boolean isMaster = false;

    /**
     * 节点状态
     */
    private RunningStatus runningStatus = RunningStatus.MONITOR;

    /**
     * 上次获取的集群状态
     */
    private Map<String, NodeStatus> lastClusterStatus = null;

    /**
     * 最后一次切换为主节点时间
     */
    private Date lastStartMasterTime;

    /**
     * 主节点不可用事件列表
     */
    private ConcurrentLinkedQueue<String> notAvailableEvents = new ConcurrentLinkedQueue<>();
    private static final int EVENT_QUEUE_MAX = 100;

    public String getNodeId() {
        return nodeId;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(RunningStatus runningStatus) {
        this.runningStatus = runningStatus;
    }

    public synchronized RunningStatus getAndSetRunningStatus(RunningStatus v0, RunningStatus v1){

        RunningStatus old = getRunningStatus();

        if(old == v0){
            setRunningStatus(v1);
        }

        return old;
    }

    public Map<String, NodeStatus> getLastClusterStatus() {
        return lastClusterStatus;
    }

    public void setLastClusterStatus(Map<String, NodeStatus> lastClusterStatus) {
        this.lastClusterStatus = lastClusterStatus;
    }

    public Date getLastStartMasterTime() {
        return lastStartMasterTime;
    }

    public void setLastStartMasterTime(Date lastStartMasterTime) {
        this.lastStartMasterTime = lastStartMasterTime;
    }

    /**
     * 添加主节点不可用事件
     * @param event
     */
    public void addNotAvailableEvent(String event){
        notAvailableEvents.add(event);

        // 清理较旧的事件
        int overflow = notAvailableEvents.size() - EVENT_QUEUE_MAX;
        if(overflow > 0){
            for(int i = 0; i < overflow; i++){
                notAvailableEvents.poll();
            }
        }
    }

    /**
     * 获取所有主节点不可用事件
     * @return
     */
    public String[] getNotAvailableEvents(){
        return notAvailableEvents.toArray(new String[0]);
    }
}
