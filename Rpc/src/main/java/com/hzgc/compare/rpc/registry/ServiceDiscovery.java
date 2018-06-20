package com.hzgc.compare.rpc.registry;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServiceDiscovery extends ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private volatile List<String> workerList = new ArrayList<String>();

    public ServiceDiscovery(String zkAddress) {
        super(zkAddress);
        initPathCache(zkClient);
    }

    private void initPathCache(CuratorFramework zkClient) {
        final PathChildrenCache pathCache =
                new PathChildrenCache(zkClient, Constant.ZK_REGISTRY_ROOT_NAMESPACE, true);
        try {
            pathCache.start();
            pathCache.getListenable().addListener((client, event) -> {
                logger.info("Child event [type:{}, path:{}]",
                        event.getType(),
                        event.getData().getPath());
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
                newWorkerList.add(event.getPath());
            });
            this.workerList = newWorkerList;
        }
    }
}
