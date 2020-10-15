package com.github.lock.interceptor;


import com.github.lock.Lock;
import com.github.lock.annotation.DistributeLock;
import com.github.lock.exception.LockedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 拦截
 * @author stephen
 * @date 2020/9/11 6:06 下午
 */
@Aspect
@Component
@Order
public class DistributeLockAspect {

    @Autowired
    private Lock lock;

    @Pointcut("@annotation(com.github.lock.annotation.DistributeLock)")
    private void distributeLock() {}

    @Around("distributeLock()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);

        String value = distributeLock.value();
        if (value == null || "".equals(value)) {
            value = point.getThis().getClass().getName() + "#" + method.getName();
        }
        String key = value + distributeLock.key();
        String guid = String.valueOf(Thread.currentThread().getId());
        boolean locked;
        if (distributeLock.block()) {
            locked = lock.blockLock(key, guid);
        } else {
            locked = lock.notBlockLock(key, guid);
        }
        if (locked) {
            Object obj = point.proceed();
            lock.release(key, guid);
            return obj;
        }
        throw new LockedException(distributeLock.errorMsg());
    }
}
