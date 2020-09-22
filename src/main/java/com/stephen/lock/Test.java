package com.stephen.lock;

import java.lang.reflect.Method;

/**
 * @Description:
 * @Author MengQingHao
 * @Date 2020/9/11 5:26 下午
 */
public class Test {


    public static void main(String[] args) {
        System.out.println(Test.class.getName());
        for (Method method : Test.class.getMethods()) {
            System.out.println(method.getName());
        }
    }
}
