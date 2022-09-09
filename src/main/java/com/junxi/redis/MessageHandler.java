package com.junxi.redis;

/**
 * 消息处理器
 * 只需要在想接受消息的服务器上的实现该方法就行
 */
public interface MessageHandler {

    /**
     * 处理收到的消息
     * @param message
     */
    void handle(String message);

}
