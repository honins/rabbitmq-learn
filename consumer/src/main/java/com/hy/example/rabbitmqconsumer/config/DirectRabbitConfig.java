package com.hy.example.rabbitmqconsumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by hy
 * @date on 2020/11/20 13:55
 */
@Configuration
public class DirectRabbitConfig {

    @Bean
    public Queue testDirectQueue() {
        return new Queue("testDirectQueue",true);
    }

    @Bean
    DirectExchange testDirectExchange() {
        return new DirectExchange("testDirectExchange");
    }

    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(testDirectQueue()).to(testDirectExchange()).with("testDirectRouting");
    }

}
