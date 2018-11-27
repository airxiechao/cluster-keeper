package com.airxiechao.clusterkeeper.service;

import com.airxiechao.clusterkeeper.repository.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 节点内运行的应用程序相关方法
 */
@Service
public class AppService {

    private static final Logger logger = LoggerFactory.getLogger(AppService.class);

    @Autowired
    private DataStore dataStore;

    /**
     * 返回是否可用
     * @return
     */
    public boolean getAvailable(){

        /**
         * TODO：添加检查主节点功能是否在运行的代码
         */
        return true;
    }

    /**
     * 返回是否至少有一个主节点服务可用
     * @return
     */
    public boolean isSomeoneOfMasterAvailable(){

        /**
         * TODO：添加检查是否有主节点某些功能在运行的代码
         */
        return false;
    }

    /**
     * 设置是否可用
     * @param available
     */
    public void setAvailable(boolean available){
        /**
         * TODO：添加启动或停止主节点功能的代码
         */
    }

}
