package com.stephen.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Description: zookeeper基础设置
 * @Author MengQingHao
 * @Date 2020/9/14 11:48 上午
 */
public class ZookeeperBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperBase.class);

    @Value("${zookeeper.server}")
    private String zookeeperServer;
    @Value("${zookeeper.baseSleepTimeMs}")
    private int baseSleepTimeMs;
    @Value("${zookeeper.sessionTimeoutMs}")
    private int sessionTimeoutMs;
    @Value("${zookeeper.maxRetries}")
    private int maxRetries;
    @Value("${zookeeper.namespace}")
    private String namespace;
    @Value("${zookeeper.rootPath}")
    protected String rootPath;

    protected CuratorFramework curatorFramework;

    public ZookeeperBase() throws Exception {
        LOGGER.debug("zookeeper init begin...................");
        init();
        LOGGER.debug("zookeeper init end...................");
    }

    private void init() throws Exception {
        //创建重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);

        //创建zookeeper客户端
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zookeeperServer)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();

        client.start();
        if (client.checkExists().forPath(rootPath)==null){
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(rootPath);
        }
    }

}
