package com.github.lock;

/**
 * 锁
 * @author stephen
 */
public interface Lock {

    /**
     * 阻塞获取锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @return boolean
     * @author stephen
     */
    boolean blockLock(String key, String guid);

    /**
     * 非阻塞获取锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @return boolean
     * @author stephen
     */
    boolean notBlockLock(String key, String guid);

    /**
     * 释放锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @author stephen
     */
    void release(String key, String guid);

}
