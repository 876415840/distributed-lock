package com.stephen.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 基于zookeeper实现的锁
 * @Author MengQingHao
 * @Date 2020/9/14 11:41 上午
 */
@Service("zookeeper-lock")
public class ZookeeperLock implements Lock {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperLock.class);

    public static final String ROOT_PATH = "distribute_lock";

    public static final String SEPARATOR = "/";

    public static final String CHILD_PREFIX = "_lock_child_";

    private Map<String, Object> lockMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private CuratorFramework curatorFramework;

    public ZookeeperLock() throws Exception {
        LOGGER.info("zookeeper init begin...................");
        init();
        LOGGER.info("zookeeper init end...................");
    }

    private void init() throws Exception {
        // TODO:MQH 2020/9/28 选择不同配置使用不同的锁实现 
        if (curatorFramework == null) {
            return;
        }
        if (curatorFramework.checkExists().forPath(ROOT_PATH)==null){
            curatorFramework.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(ROOT_PATH);
        }
    }

    @Override
    public boolean blockLock(String key, String guid) {
        final String parentPath = joinKey(key);
        lockMap.computeIfAbsent(parentPath, k -> new Object());
        String path = parentPath + SEPARATOR + CHILD_PREFIX;

        try {
            String node = curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, getData(guid));
            LOGGER.debug("[block lock] node create success. path[{}] node[{}]", path, node);
            lock(parentPath, Integer.parseInt(node.replace(path, "")));
            return true;
        } catch (Exception e) {
            LOGGER.error("[block lock] node create error. path[{}]", path, e);
            return false;
        }
    }

    private void lock(final String parentPath, int centerNodeSort) throws Exception {
        while (true) {
            List<String> childs = curatorFramework.getChildren().forPath(parentPath);
            // 有子节点比当前子节点更早创建
            boolean locked = childs.stream().anyMatch(nodeName -> Integer.parseInt(nodeName.replace(CHILD_PREFIX, "")) < centerNodeSort);
            if (!locked) {
                LOGGER.debug("lock success. parentPath[{}]", parentPath);
                return;
            }
            // 注册父节点监听事件
            Object obj = lockMap.get(parentPath);
            synchronized(obj) {
                listenerNode(parentPath);
                obj.wait();
            }
        }
    }

    @Override
    public boolean notBlockLock(String key, String guid) {
        String path = joinKey(key);
        try {
            String node = curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, getData(guid));
            LOGGER.debug("[not block lock] node create success. path[{}] node[{}]", path, node);
            return true;
        } catch (Exception e) {
            LOGGER.error("[not block lock] node create error. path[{}]", path, e);
            return false;
        }
    }

    /**
     * 监听父节点下的节点删除动作，触发后唤醒所有线程
     * @param parentPath
     * @return void
     * @author MengQingHao
     * @date 2020/9/21 10:50 上午
     */
    private void listenerNode(String parentPath) {
        TreeCache treeCache = new TreeCache(curatorFramework, parentPath);
        treeCache.getListenable().addListener((client, event) -> {
            ChildData eventData = event.getData();
            switch (event.getType()) {
                case NODE_ADDED:
                    LOGGER.warn("add node[{}] data[{}]", eventData.getPath(), new String(eventData.getData()));
                    break;
                case NODE_UPDATED:
                    LOGGER.warn("update node[{}] data[{}]", eventData.getPath(), new String(eventData.getData()));
                    break;
                case NODE_REMOVED:
                    LOGGER.warn("update node[{}]", eventData.getPath());
                    lockMap.get(parentPath).notifyAll();
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public boolean release(String key, String guid) {
        String path = joinKey(key);
        try {
            List<String> childs = curatorFramework.getChildren().forPath(path);
            if (childs.isEmpty()) {
                return deleteNode(guid, path);
            }
            for (String child : childs) {
                if (deleteNode(guid, child)) {
                    return true;
                }
            }
            LOGGER.error("[release] node delete error. not find. path[{}] guid[{}]", path, guid);
            return false;
        } catch (Exception e) {
            LOGGER.error("[release] node delete error. path[{}]", path, e);
            return false;
        }
    }

    private boolean deleteNode(String guid, String path) throws Exception {
        byte[] data = curatorFramework.getData().forPath(path);
        if (guid == null && data == null) {
            LOGGER.debug("[release] node delete success. path[{}]", path);
            return true;
        }
        if (data == null) {
            LOGGER.error("[release] node delete error. old guid is null, new guid not null. path[{}]", path);
            return false;
        }
        String newGuid = new String(data);
        if (newGuid.equals(guid)) {
            curatorFramework.delete().forPath(path);
            LOGGER.debug("[release] node delete success. path[{}]", path);
            return true;
        } else {
            LOGGER.error("[release] node delete error. guid not equals. path[{}]. old guid[{}]. new guid[{}]", path, newGuid, guid);
            return false;
        }
    }

    private String joinKey(String key) {
        if (key == null || key.trim().length() == 0) {
            throw new NullPointerException();
        }
        StringBuilder sb = new StringBuilder(ROOT_PATH);
        if (!key.startsWith(SEPARATOR)) {
            sb.append(SEPARATOR);
        }
        sb.append(key);
        return sb.toString();
    }

    private byte[] getData(String data) {
        if (data == null) {
            return null;
        }
        return data.getBytes();
    }

}
