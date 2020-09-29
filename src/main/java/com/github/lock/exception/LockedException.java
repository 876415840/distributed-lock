package com.github.lock.exception;

/**
 * @Description: 占用锁异常
 * @Author MengQingHao
 * @Date 2020/9/23 11:38 上午
 */
public class LockedException extends RuntimeException {

    public LockedException() {
    }

    public LockedException(String message) {
        super(message);
    }

    public LockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockedException(Throwable cause) {
        super(cause);
    }
}
