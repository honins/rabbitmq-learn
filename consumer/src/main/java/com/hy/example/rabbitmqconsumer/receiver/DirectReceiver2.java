package com.hy.example.rabbitmqconsumer.receiver;

import com.hy.example.rabbitmqprovider.dto.LogMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Created by hy
 * @date on 2020/11/20 13:56
 */
@Component
@RabbitListener(queues = "testDirectQueue")
public class DirectReceiver2 {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("2  DirectReceiver消费者收到消息  : " + logMessage.toString());
    }

}