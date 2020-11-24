package com.hy.example.rabbitmqprovider.controller;

import com.hy.example.rabbitmqprovider.dto.LogMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author Created by hy
 * @date on 2020/11/20 13:41
 */
@RestController
public class SendMessageController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage() {
        LogMessage logMessage = new LogMessage();
        logMessage.setId(0L);
        logMessage.setMsg("direct message, hello!");
        logMessage.setLogLevel("error");
        logMessage.setServiceType("server provider");
        logMessage.setCreateTime(new Date());
        logMessage.setUserId(0L);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("testDirectExchange", "testDirectRouting", logMessage);
        return "ok";
    }

    @GetMapping("/sendTopicMessage1")
    public String sendTopicMessage1() {
        LogMessage logMessage = new LogMessage();
        logMessage.setId(0L);
        logMessage.setMsg("topic message, hello!");
        logMessage.setLogLevel("error");
        logMessage.setServiceType("server provider");
        logMessage.setCreateTime(new Date());
        logMessage.setUserId(0L);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("topicExchange", "topicKey1", logMessage);
        return "ok";
    }

    @GetMapping("/sendTopicMessage2")
    public String sendTopicMessage2() {
        LogMessage logMessage = new LogMessage();
        logMessage.setId(0L);
        logMessage.setMsg("topic message, hello!");
        logMessage.setLogLevel("error");
        logMessage.setServiceType("server provider");
        logMessage.setCreateTime(new Date());
        logMessage.setUserId(0L);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("topicExchange", "topicKey2", logMessage);
        return "ok";
    }

    @GetMapping("/sendFanoutMessage")
    public String sendFanoutMessage() {
        LogMessage logMessage = new LogMessage();
        logMessage.setId(0L);
        logMessage.setMsg("topic message, hello!");
        logMessage.setLogLevel("error");
        logMessage.setServiceType("server provider");
        logMessage.setCreateTime(new Date());
        logMessage.setUserId(0L);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("fanoutExchange", null, logMessage);
        return "ok";
    }

}
