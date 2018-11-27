package com.airxiechao.clusterkeeper.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.airxiechao.clusterkeeper.repository.DataStore;
import com.airxiechao.clusterkeeper.repository.NodeStatus;
import com.airxiechao.clusterkeeper.service.NodeService;
import com.airxiechao.clusterkeeper.util.SecurityUtil;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向节点发出请求的客户端
 */
@Component
public class NodeClient {

    private Logger logger = LoggerFactory.getLogger(NodeClient.class);

    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private DataStore dataStore;

    @Autowired
    private NodeService nodeService;

    /**
     * 下载文件夹
     */
    private final String DOWNLOAD_DIR = "downloads";

    /**
     * 请求节点ID
     * @param node
     * @return
     */
    public String queryNodeId(String node){
        String url = getURL(node,"/node/id");
        Request request = buildRequest(url);

        String nodeId = "";
        try {
            Response response = client.newCall(request).execute();
            nodeId = response.body().string();
        } catch (Exception e) {
        }

        return nodeId;
    }

    /**
     * 请求节点信息
     * @param node
     * @return
     */
    public NodeStatus queryNodeStatus(String node){
        String url = getURL(node,"/node/status");
        Request request = buildRequest(url);

        NodeStatus nodeStatus = new NodeStatus();
        try {
            Response response = client.newCall(request).execute();
            nodeStatus = mapper.readValue(response.body().string(), NodeStatus.class);
        } catch (Exception e) {

        }

        return nodeStatus;
    }

    /**
     * 请求切换自己为主节点
     * @param node
     * @return
     */
    public boolean queryRequestMasterChange(String node){
        String url = getURL(node,"/master/request");
        Request request = buildRequest(url);

        boolean agree = false;
        try {
            Response response = client.newCall(request).execute();
            agree = mapper.readValue(response.body().string(), Boolean.class);

        } catch (Exception e) {
        }

        return agree;
    }

    /**
     * 通知主节点已切换
     * @param node
     */
    public void queryMasterChanged(String node){
        String url = getURL(node,"/master/changed");
        Request request = buildRequest(url);

        try {
            client.newCall(request).execute();
        } catch (Exception e) {

        }
    }

    /**
     * 请求集群状态
     * @return
     */
    public Map<String, NodeStatus> queryClusterStatus(){
        Map<String, NodeStatus> clusterStatus = new HashMap<>();

        List<String> nodes = nodeService.getNodeList();
        for(String node : nodes){
            NodeStatus nodeStatus;

            String nodeId = queryNodeId(node);
            if(nodeId.equals(dataStore.getNodeId())){
                nodeStatus = nodeService.getNodeStatus();
            }else{
                nodeStatus = queryNodeStatus(node);
            }

            clusterStatus.put(node, nodeStatus);
        }

        dataStore.setLastClusterStatus(clusterStatus);
        return clusterStatus;
    }

    private Request buildRequest(String url){
        Request request = new Request.Builder()
                .url(url)
                .header(SecurityUtil.NODE_TOKEN_HEADER, SecurityUtil.encryptNodeToken())
                .build();

        return request;
    }

    private String getURL(String node, String path){
        return "http://"+node+path;
    }
}
