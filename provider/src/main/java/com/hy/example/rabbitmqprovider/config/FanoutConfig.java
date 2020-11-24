package com.hy.example.rabbitmqprovider.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by hy
 * @date on 2020/11/23 14:50
 */
@Configuration
public class FanoutConfig {

    @Bean
    public Queue queueA(){
        return new Queue("queueA");
    }

    @Bean
    public Queue queueB(){
        return new Queue("queueB");
    }

    @Bean
    public Queue queueC(){
        return new Queue("queueC");
    }

    @Bean
    public FanoutExchange exchange(){
        return ExchangeBuilder.fanoutExchange("fanoutExchange").build();
    }

    @Bean
    Binding bindingDirectA() {
        return BindingBuilder.bind(queueA()).to(exchange());
    }

    @Bean
    Binding bindingDirectB() {
        return BindingBuilder.bind(queueB()).to(exchange());
    }

    @Bean
    Binding bindingDirectC() {
        return BindingBuilder.bind(queueC()).to(exchange());
    }
}
