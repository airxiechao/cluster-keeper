# cluster-keeper
简易单主节点集群管理工具

## 功能简介：
- 监测集群各个节点状态
- 协商产生唯一主节点
- 发现主节点不可用时，自动切换主节点

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
