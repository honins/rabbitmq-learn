package com.hy.example.rabbitmqconsumer.receiver;

import com.hy.example.rabbitmqprovider.dto.LogMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author Created by hy
 * @date on 2020/11/23 14:13
 */
@Component
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(value = "topicQueue"),
                exchange = @Exchange(value = "topicExchange", type = ExchangeTypes.TOPIC),
                key = "topic*"
        )
)
public class TopicReceiver1 {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("1  收到topic消息：" + logMessage.toString());
    }

}
