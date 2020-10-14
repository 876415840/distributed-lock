### zookeeper实现分布式锁

> 通过zk的临时节点实现分布式锁

* maven依赖
```xml
<dependency>
    <groupId>com.github.876415840</groupId>
    <artifactId>distributed-lock</artifactId>
    <version>1.0.0</version>
</dependency>
```

* zookeeper配置
```java 
@Configuration
public class ZookeeperConfig {

    @Bean
    public CuratorFramework curatorFramework() {
        //创建重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,5);

        //创建zookeeper客户端
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
                .sessionTimeoutMs(180000)
                .retryPolicy(retryPolicy)
                .namespace("your_namespace")
                .build();

        client.start();
        return client;
    }
}
```

* 锁使用
```java
// 启动类加注解 @ComponentScan(basePackages = {"com.github.lock"}) 扫描指定包路径

@Service
public class TestService {
    @DistributeLock(value = "testLock", key = "#id", block = true)
    public void testLock(String id) {
        // do something
    }
}
```
