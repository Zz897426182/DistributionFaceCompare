package com.hzgc.compare.rpc.client.connect;

import com.google.common.collect.Lists;
import com.hzgc.compare.rpc.zookeepr.Constant;
import com.hzgc.compare.rpc.zookeepr.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceDiscovery extends ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private volatile List<String> workerList = new ArrayList<String>();
    //启动时存在多次启动的情况，需要解决
    public ServiceDiscovery(String zkAddress) {
        super(zkAddress);
        initPathCache(zkClient);
        updateConnectedServer();
    }

    private void initPathCache(CuratorFramework zkClient) {
        final PathChildrenCache pathCache =
                new PathChildrenCache(zkClient, Constant.ZK_REGISTRY_ROOT_PATH, true);
        try {
            pathCache.start();
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
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void refreshData(List<ChildData> currenDataList) {
        if (currenDataList != null && !currenDataList.isEmpty()) {
            final List<String> newWorkerList = Lists.newArrayList();
            currenDataList.forEach(event -> {
                newWorkerList.add(new String(event.getData()));
            });
            this.workerList = newWorkerList;
            updateConnectedServer();
        }
    }

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
