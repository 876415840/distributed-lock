package com.github.lock.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {

    /**
     * key前缀
     * 没有设值情况，取class.getName() + # + method.getName()
     */
    String value() default "";

    /**
     * key后缀
     */
    String key() default "";

    /**
     * 是否阻塞
     */
    boolean block() default false;

    /**
     * 锁失败异常消息
     */
    String errorMsg() default "locked failed.";
}
