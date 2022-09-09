package com.junxi.redis;


import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;

/**
 * 消息发布者
 * 如果消息接收者的channel和发布者的channel是同一个channel，则消息发布者发布消息时，接收者就能收到
 */
public class Publisher {

    private final String channel;

    private final Jedis jedis;

    private Publisher(Jedis jedis,String channel){
        this.jedis = jedis;
        this.channel = channel;
    }

    /**
     * 返回一个指定了channel的发布者
     * @param jedis
     * @param channel
     * @return
     */
    public static final Publisher getPublisher(Jedis jedis,String channel){
        return new Publisher(jedis,channel);
    }

    /**
     * 发送文本消息
     * @param message
     * @return
     */
    public Long publish(String message){
        return jedis.publish(channel, message);
    }

    /**
     * 发送json格式的消息
     * @param obj
     * @return
     */
    public Long publishJson(Object obj){
        return jedis.publish(channel, JSON.toJSONString(obj));
    }

}
