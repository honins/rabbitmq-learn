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
@RabbitListener(queues = "queueC")
public class FanoutReceiverC {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("C  收到queueC消息：" + logMessage.toString());
    }

}
