package com.stephen.lock;

/**
 * @Description: 锁
 * @Author MengQingHao
 * @Date 2020/9/14 11:29 上午
 */
public interface Lock {

    /**
     * 阻塞获取锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @return boolean
     * @author MengQingHao
     * @date 2020/9/14 11:32 上午
     */
    boolean blockLock(String key, String guid);

    /**
     * 非阻塞获取锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @return boolean
     * @author MengQingHao
     * @date 2020/9/14 11:32 上午
     */
    boolean notBlockLock(String key, String guid);

    /**
     * 释放锁
     * @param key 锁的唯一标识
     * @param guid 客户端的唯一标识
     * @return boolean
     * @author MengQingHao
     * @date 2020/9/14 11:34 上午
     */
    boolean release(String key, String guid);

}
