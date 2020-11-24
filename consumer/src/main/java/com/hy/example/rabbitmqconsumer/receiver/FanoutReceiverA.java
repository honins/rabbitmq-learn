package com.hy.example.rabbitmqconsumer.receiver;

import com.hy.example.rabbitmqprovider.dto.LogMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Created by hy
 * @date on 2020/11/23 14:13
 */
@Component
@RabbitListener(queues = "queueA")
public class FanoutReceiverA {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("A  收到queueA消息：" + logMessage.toString());
    }

}
