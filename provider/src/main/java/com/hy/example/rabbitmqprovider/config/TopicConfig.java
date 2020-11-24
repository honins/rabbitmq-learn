package com.hy.example.rabbitmqprovider.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by hy
 * @date on 2020/11/23 14:02
 */
@Configuration
public class TopicConfig {

    @Bean
    public Queue topicQueue() {
        return new Queue("topicQueue");
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("topicExchange");
    }

    @Bean
    public Binding bind1() {
        return BindingBuilder.bind(topicQueue()).to(topicExchange()).with("topicKey1");
    }

    @Bean
    public Binding bind2() {
        return BindingBuilder.bind(topicQueue()).to(topicExchange()).with("topicKey2");
    }
}
