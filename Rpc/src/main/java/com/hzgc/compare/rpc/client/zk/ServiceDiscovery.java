package com.hzgc.compare.rpc.client.zk;

import com.google.common.collect.Lists;
import com.hzgc.compare.rpc.client.ConnectManager;
import com.hzgc.compare.rpc.util.Constant;
import com.hzgc.compare.rpc.util.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用于节点发现,用来监听zk节点下服务端注册情况，当出现子节点添加或者子节点删除等事件
 * 将会通知ConnectManager来刷新连接
 */
public class ServiceDiscovery extends ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private volatile List<String> workerList = new ArrayList<>();
    public ServiceDiscovery(String zkAddress) {
        super(zkAddress);
        initPathCache(zkClient);
    }

    /**
     * 初始化节点缓存,这里的PathChildrenCache可以理解为Zookeeper的Watcher的封装
     *
     * @param zkClient zookeeper客户端
     */
    private void initPathCache(CuratorFramework zkClient) {
        final PathChildrenCache pathCache =
                new PathChildrenCache(zkClient, Constant.ZK_REGISTRY_ROOT_PATH, true);
        try {
            //此种类型的StartMode意思为已存在节点不作为变化事件
            pathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathCache.getListenable().addListener((client, event) -> {
                logger.info("Child event [type:{}, path:{}]",
                        event.getType(),
                        event.getData() != null ? event.getData().getPath() : null);
                switch (event.getType()) {
                    case CHILD_ADDED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    case CHILD_UPDATED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    case CHILD_REMOVED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    default:
                        break;
                }
            });
            //尝试第一次刷新节点下数据
            refreshData(pathCache.getCurrentData());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 刷新节点数据,将ChildData对象集合转为节点数据集合
     * 刷新节点数据,将ChildData对象集合转为节点数据集合,同时更新服务端连接信息
     *
     * @param currenDataList ChildData对象集合,表示当前节点下的数据
     */
    private void refreshData(List<ChildData> currenDataList) {
        if (currenDataList != null && !currenDataList.isEmpty()) {
            final List<String> newWorkerList = Lists.newArrayList();
            currenDataList.forEach(event -> {
                newWorkerList.add(new String(event.getData()));
            });
            this.workerList = newWorkerList;
            //更新服务端连接信息
            updateConnectedServer();
        }
    }

    /**
     * 此方法会调用ConnectManager的updateConnectedServer方法用来更新连接信息
     */
    private void updateConnectedServer() {
        logger.info("Service discovery triggered updating connected server nodes:{}", Arrays.toString(this.workerList.toArray()));
        if (this.workerList.size() > 0) {
            ConnectManager.getInstance().updateConnectedServer(this.workerList);
        }
    }

    public void stop() {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
