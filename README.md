# cluster-keeper
简易单主节点集群管理工具

## 功能简介：
- 监测集群各个节点状态
- 协商产生唯一主节点
- 发现主节点不可用时，自动切换主节点

## 实现原理：
每个节点遍历获取所有节点状态，如果发现没有可用主节点，就向其他在线节点请求切换自己为主节点，得到所有其他在线节点的同意后，就启动自己的主节点相关服务，标记自己为主节点，通知其他在线节点完成了主节点切换。节点可能有4种状态（监控、已发出切换自己为主节点请求、已同意其他节点切换主节点请求、离线），和响应2种请求（其他节点请求切换自己为主节点、其他节点通知已完成主节点切换），不同状态下的响应不同。

## 定制方法：
在文件com.airxiechao.clusterkeeper.service.AppService中，写入检测、启动、停止主节点相关功能的代码。

## 编译打包：
使用mvn package打包，生成cluster-keeper.jar。

## 配置运行
编写节点列表配置文件node.conf，每行一个节点IP地址，带上端口9000，例如：
```
192.168.1.100:9000
192.168.1.101:9000
```
将cluster-keeper.jar和node.conf拷贝到各个节点，在每个节点使用java -jar cluster-keeper.jar启动。访问任意节点IP:9000，可查看集群状态。
