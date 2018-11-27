package com.airxiechao.clusterkeeper.task;

import com.airxiechao.clusterkeeper.repository.DataStore;
import com.airxiechao.clusterkeeper.repository.NodeStatus;
import com.airxiechao.clusterkeeper.service.AppService;
import com.airxiechao.clusterkeeper.service.NodeService;
import com.airxiechao.clusterkeeper.web.NodeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 节点定时任务
 */
@Component
public class NodeTask {

    private static final Logger logger = LoggerFactory.getLogger(NodeTask.class);

    @Autowired
    private DataStore dataStore;

    @Autowired
    private NodeClient nodeClient;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AppService appService;

    private Random random = new Random();

    /**
     * 定时检测集群状态
     */
    @Scheduled(fixedDelayString = "${monitor.delay}")
    public void monitor(){

        try {
            // 随机3秒内延时
            Thread.sleep(random.nextInt(3000));
        } catch (InterruptedException e) {

        }

        // 没有节点列表，退出
        if(!nodeService.hasNodeList()){
            return;
        }

        logger.info("-------------集群定时检测开始-------------");

        List<String> nodeList = nodeService.getNodeList();
        logger.info("集群所有配置节点：{}", nodeList);

        NodeStatus status = nodeService.getNodeStatus();
        logger.info("当前节点：{}, 节点ID：{}, 是否为主节点{}, 可用性为{}",
                nodeService.getOwnNode(), status.getNodeId(), status.isMaster(), status.isAvailable());

        synchronized (dataStore){

            // 检查或切换主节点
            try{
                nodeService.checkOrStartMaster(false);
            }catch (Exception e){
                logger.error("检查或切换主节点发生错误", e);
            }

            // 检查并修复自身节点的服务状态
            try{
                nodeService.checkAndRepairNode();
            }catch (Exception e){
                logger.error("检查并修复自身节点的服务状态发生错误", e);
            }

        }
        logger.info("集群定时检测结束");
    }
}
