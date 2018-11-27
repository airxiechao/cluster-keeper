package com.airxiechao.clusterkeeper.job.sysperf.service;

import com.airxiechao.clusterkeeper.job.sysperf.entity.SystemPerf;

public interface SystemPerfService {

    /**
     * 获取当前性能
     * @return
     */
    SystemPerf getCurrentPerf();
}
