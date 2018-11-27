package com.airxiechao.clusterkeeper.service;

import com.airxiechao.clusterkeeper.job.sysperf.service.SystemPerfService;
import com.airxiechao.clusterkeeper.repository.DataStore;
import com.airxiechao.clusterkeeper.repository.NodeStatus;
import com.airxiechao.clusterkeeper.repository.RunningStatus;
import com.airxiechao.clusterkeeper.web.NodeClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 节点相关功能方法
 */
@Service
public class NodeService {

    private Logger logger = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private DataStore dataStore;

    @Autowired
    private NodeClient nodeClient;

    @Autowired
    private AppService appService;

    @Autowired
    private SystemPerfService systemPerfService;

    @Value("${node.txt.path}")
    private String nodeTxtPath;

    /**
     * 获取节点信息
     * @return
     */
    public NodeStatus getNodeStatus(){
        NodeStatus status = new NodeStatus();
        status.setNodeId(dataStore.getNodeId());
        status.setAvailable(appService.getAvailable());
        status.setMaster(dataStore.isMaster());
        status.setRunningStatus(dataStore.getRunningStatus());
        status.setSystemPerf(systemPerfService.getCurrentPerf());
        status.setLastStartMasterTime(dataStore.getLastStartMasterTime());
        status.setNotAvailableEvents(dataStore.getNotAvailableEvents());
        status.setNodeList(getNodeList());

        return status;
    }

    /**
     * 响应其他节点切换主节点的请求
     * @return
     */
    public boolean respondMasterChange(){
        if(dataStore.getAndSetRunningStatus(RunningStatus.MONITOR, RunningStatus.AGREE) == RunningStatus.MONITOR){
            logger.info("同意主节点切换");
            return true;
        }

        logger.info("拒绝主节点切换");
        return false;
    }

    /**
     * 得知主节点已切换
     */
    public void knowMasterChanged(){
        logger.info("得知主节点已切换");
        dataStore.getAndSetRunningStatus(RunningStatus.AGREE, RunningStatus.MONITOR);
        stopMaster();
    }

    /**
     * 启动自己作为主节点
     */
    public void startMaster(){
        appService.setAvailable(true);
        dataStore.setMaster(true);
        dataStore.setLastStartMasterTime(new Date());
    }

    /**
     * 停止自己作为主节点
     */
    public void stopMaster(){
        appService.setAvailable(false);
        dataStore.setMaster(false);
    }

    /**
     * 根据集群状态，按需启动自己作为主节点
     * @param force 不管集群主节点是否可用，强制启动自己为主节点
     */
    public void checkOrStartMaster(boolean force){
        Map<String, NodeStatus> clusterStatus = nodeClient.queryClusterStatus();

        List<String> masters = new ArrayList<>();
        List<String> notAvailableMasters = new ArrayList<>();
        List<String> otherOnlineNodes = new ArrayList<>();
        for(Map.Entry<String, NodeStatus> entry : clusterStatus.entrySet()){
            if(entry.getValue().isMaster() && entry.getValue().isAvailable()){
                masters.add(entry.getKey());
            }

            if(entry.getValue().isMaster() && !entry.getValue().isAvailable()){
                notAvailableMasters.add(entry.getKey());
            }

            if(entry.getValue().getRunningStatus() != RunningStatus.OFFLINE &&
                    entry.getValue().getNodeId() != dataStore.getNodeId()){
                otherOnlineNodes.add(entry.getKey());
            }
        }

        logger.info("发现可用主节点：{}", masters);
        logger.info("发现其他在线节点：{}", otherOnlineNodes);

        recordNotAvailableEvent(masters, notAvailableMasters);

        boolean changeMaster = masters.size() == 0 || masters.size() > 1;
        if(force || changeMaster){
            if(dataStore.getAndSetRunningStatus(RunningStatus.MONITOR, RunningStatus.REQUEST) == RunningStatus.MONITOR){
                boolean agree = true;
                for(String node : otherOnlineNodes){
                    logger.info("询问节点是否同意切换自己为主节点："+node);
                    if(clusterStatus.get(node).getRunningStatus() == RunningStatus.REQUEST){
                        agree = false;
                    }

                    if(!nodeClient.queryRequestMasterChange(node)) {
                        agree = false;
                    }

                    if(!agree){
                        logger.info("节点拒绝："+node);
                        break;
                    }else{
                        logger.info("节点同意："+node);
                    }
                }

                // 切换
                if(agree){
                    logger.info("切换自己为主节点");
                    startMaster();

                    for(String node : otherOnlineNodes){
                        logger.info("通知节点自己切换为主节点："+node);
                        nodeClient.queryMasterChanged(node);
                    }
                }

                dataStore.setRunningStatus(RunningStatus.MONITOR);
            }

        }
    }

    /**
     * 检查并修复自身节点运行状态
     */
    public void checkAndRepairNode(){

        // 检查并修复非主节点运行状态
        if(!dataStore.isMaster()){
            if(appService.isSomeoneOfMasterAvailable()){
                logger.info("发现自身非主节点，但有主节点某些服务正在运行，停止这些服务");
                stopMaster();
            }
        }

        dataStore.getAndSetRunningStatus(RunningStatus.AGREE, RunningStatus.MONITOR);
        dataStore.getAndSetRunningStatus(RunningStatus.REQUEST, RunningStatus.MONITOR);
    }

    /**
     * 获取节点列表
     * @return
     */
    public List<String> getNodeList(){
        List<String> nodes = new ArrayList<>();

        File nodeTxtFile = new File(nodeTxtPath);
        if(nodeTxtFile.exists()){
            try(BufferedReader br = new BufferedReader(new FileReader(nodeTxtFile))){
                String line;
                while((line = br.readLine()) != null){
                    line = line.trim();
                    if(!line.isEmpty() && !line.startsWith("#")){
                        nodes.add(line);
                    }
                }
            } catch (Exception e) {
                logger.error("获取节点列表发生错误", e);
            }
        }

        return nodes;
    }

    /**
     * 获取集群中的主节点
     * @return
     */
    public String getMasterNode(){
        String masterNode = null;
        Map<String, NodeStatus> clusterStatus = dataStore.getLastClusterStatus();
        if(null == clusterStatus){
            clusterStatus = nodeClient.queryClusterStatus();
        }

        for(Map.Entry<String, NodeStatus> entry : clusterStatus.entrySet()){
            String node = entry.getKey();
            NodeStatus status = entry.getValue();
            if(status.isMaster() && status.isAvailable()){
                masterNode = node;
                break;
            }
        }

        return masterNode;
    }

    /**
     * 获取自己节点地址
     * @return
     */
    public String getOwnNode(){
        String ownNode = null;
        Map<String, NodeStatus> clusterStatus = dataStore.getLastClusterStatus();
        if(null == clusterStatus){
            clusterStatus = nodeClient.queryClusterStatus();
        }

        for(Map.Entry<String, NodeStatus> entry : clusterStatus.entrySet()){
            String node = entry.getKey();
            NodeStatus status = entry.getValue();
            if(status.getNodeId().equals(dataStore.getNodeId())){
                ownNode = node;
                break;
            }
        }

        return ownNode;
    }

    /**
     * 检查是否有节点列表
     * @return
     */
    public boolean hasNodeList(){
        List<String> nodeList = getNodeList();
        if(null != nodeList && nodeList.size() > 0){
            return true;
        }

        return false;
    }

    /**
     * 记录主节点不可用事件
     * @param availableMasters
     * @param notAvailableMasters
     */
    private void recordNotAvailableEvent(List<String> availableMasters, List<String> notAvailableMasters){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = formatter.format(new Date());

        List<String> evs = new ArrayList<>();

        if(null != availableMasters){
            if(availableMasters.size() == 0){
                evs.add(String.format("无可用主节点"));
            }else if(availableMasters.size() > 1){
                evs.add(String.format("发现多个可用主节点：%s", StringUtils.join(availableMasters, ",")));
            }
        }

        if(null != notAvailableMasters && notAvailableMasters.size() > 0){
            evs.add(String.format("发现不可用主节点：%s", StringUtils.join(notAvailableMasters, ",")));
        }

        if(evs.size() > 0){
            dataStore.addNotAvailableEvent(String.format("%s - %s", now, StringUtils.join(evs, "，")));
        }

    }
}