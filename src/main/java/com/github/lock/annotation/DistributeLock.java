package com.github.lock.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {

    /**
     * 没有设值情况，取class.getName() + # + method.getName()
     * @return key前缀
     */
    String value() default "";

    /**
     * @return key后缀
     */
    String key() default "";

    /**
     * @return 是否阻塞
     */
    boolean block() default false;

    /**
     * @return 锁失败异常消息
     */
    String errorMsg() default "locked failed.";
}
