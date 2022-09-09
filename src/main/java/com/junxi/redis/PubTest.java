package com.junxi.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;

public class PubTest implements MessageHandler{

    private JedisPool jedispool;

    public PubTest(){
        GenericObjectPoolConfig<Jedis> genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMinIdle(8);
        genericObjectPoolConfig.setMaxIdle(64);
        genericObjectPoolConfig.setMaxTotal(256);
        genericObjectPoolConfig.setMaxWaitMillis(200);
        genericObjectPoolConfig.setTestWhileIdle(true);
        genericObjectPoolConfig.setTestOnBorrow(false);
        genericObjectPoolConfig.setTestOnReturn(false);

        jedispool = new JedisPool(genericObjectPoolConfig, "127.0.0.1", 6379, 1000, null);
        Jedis jedis1 = jedispool.getResource();
        //先启动订阅
        ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean = new ThreadPoolExecutorFactoryBean();
        threadPoolExecutorFactoryBean.setCorePoolSize(8);
        threadPoolExecutorFactoryBean.setMaxPoolSize(1024);
        threadPoolExecutorFactoryBean.setQueueCapacity(0);
        threadPoolExecutorFactoryBean.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolExecutorFactoryBean.setAwaitTerminationSeconds(5);
        threadPoolExecutorFactoryBean.initialize();
        ExecutorService threadPool = threadPoolExecutorFactoryBean.getObject();

        Subscriber subscriber = new Subscriber(jedis1, threadPool);
        subscriber.addMessageHandler("global_channel",this);
        subscriber.start();
    }

    public static void main(String[] args) throws InterruptedException {
        PubTest pubTest = new PubTest();
        pubTest.pub();
    }

    private void pub()throws InterruptedException{
        Jedis jedis2 = jedispool.getResource();
        Publisher pub = Publisher.getPublisher(jedis2, "global_channel");
        System.out.println("准备开始");
        Thread.sleep(5000);
        pub.publishJson("hello");
    }

    @Override
    public void handle(String message) {
        System.out.println("hello world");
        System.out.println(message);
    }
}
