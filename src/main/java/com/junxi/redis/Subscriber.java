package com.junxi.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 消息订阅者
 * 只会接收到订阅发布的消息，离线则
 */
public class Subscriber extends Thread{

    private final Jedis jedis;
    private final JedisPubSub jedisPubSub;
    private Map<String, List<MessageHandler>> handlerMap;

    public Subscriber(Jedis jedis, ExecutorService threadpool) {
        this.jedis = jedis;
        this.jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                List<MessageHandler> handlers = handlerMap.get(channel);
                if(null == handlers || handlers.size()==0){
                    return;
                }
                handlers.forEach(handler -> threadpool.execute(()->handler.handle(message)));
            }
        };
        this.handlerMap = new HashMap<>();
    }

    /**
     * 添加一个消息处理器
     * @param channel
     * @param handler
     */
    public void addMessageHandler(String channel,MessageHandler handler){

        handlerMap.computeIfAbsent(channel,k->new ArrayList<>()).add(handler);
    }

    /**
     * 取消所有订阅，退出时调用
     */
    public void unSubscribe(){
        jedisPubSub.unsubscribe(handlerMap.keySet().toArray(new String[0]));
        jedis.close();
    }

    @Override
    public void run() {
        jedis.subscribe(jedisPubSub,handlerMap.keySet().toArray(new String[0]));
    }

}
