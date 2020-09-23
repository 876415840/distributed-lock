package com.stephen.lock.annotation;

import java.lang.annotation.*;

/**
 * @Description: 所注解
 * @Author MengQingHao
 * @Date 2020/9/11 5:27 下午
 */
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
