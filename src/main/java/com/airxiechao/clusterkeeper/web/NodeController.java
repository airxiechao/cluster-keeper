package com.airxiechao.clusterkeeper.web;

import com.airxiechao.clusterkeeper.repository.DataStore;
import com.airxiechao.clusterkeeper.repository.NodeStatus;
import com.airxiechao.clusterkeeper.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 节点管理http接口
 */
@RestController
public class NodeController {

    private Logger logger = LoggerFactory.getLogger(NodeController.class);

    @Autowired
    private DataStore dataStore;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private NodeClient nodeClient;

    /**
     * 获取节点ID
     * @return
     */
    @RequestMapping("/node/id")
    public String getNodeId(){
        return dataStore.getNodeId();
    }

    /**
     * 获取节点信息
     * @return
     */
    @RequestMapping("/node/status")
    public NodeStatus getNodeStatus(){
        return nodeService.getNodeStatus();
    }

    /**
     * 获取集群状态
     * @param cache 是否使用上次记录的值，默认为否
     * @return
     */
    @RequestMapping("/cluster/status")
    public Map<String, NodeStatus> getClusterStatus(
            @RequestParam(value = "cache", required = false, defaultValue = "false") boolean cache
    ){
        Map<String, NodeStatus> clusterStatus = null;

        if(cache){
            clusterStatus = dataStore.getLastClusterStatus();
        }

        if(null == clusterStatus) {
            clusterStatus = nodeClient.queryClusterStatus();
        }
        return clusterStatus;
    }

    @RequestMapping("/proxy/cluster/status")
    public Map<String, NodeStatus> getClusterStatusByProxy(
            @RequestParam(value = "cache", required = false, defaultValue = "false") boolean cache
    ){
        return getClusterStatus(cache);
    }

    /**
     * 接收其他节点切换主节点请求
     * @return
     */
    @RequestMapping("/master/request")
    public boolean requestMasterChange(){
        return nodeService.respondMasterChange();
    }

    /**
     * 接收其他节点已切换主节点通知
     */
    @RequestMapping("/master/changed")
    public void masterChanged(){
        synchronized (dataStore){
            nodeService.knowMasterChanged();
        }
    }

    /**
     * 启动自己为主节点
     */
    @RequestMapping("/master/start")
    public void startMaster(){
        synchronized (dataStore){
            nodeService.checkOrStartMaster(true);
        }
    }

}

