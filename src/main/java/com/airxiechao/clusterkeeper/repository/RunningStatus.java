package com.airxiechao.clusterkeeper.repository;

/**
 * 节点状态枚举值
 * OFFLINE：离线
 * MONITOR：监控
 * REQUEST：已发出切换自己为主节点请求
 * AGREE：已同意其他节点切换主节点请求
 */
public enum RunningStatus {
    OFFLINE,
    MONITOR,
    REQUEST,
    AGREE,
}
