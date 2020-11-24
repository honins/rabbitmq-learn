# 消息中间件MQ

## 介绍和选型

消息队列中间件是分布式系统中重要的组件，主要解决应用耦合，异步消息，流量削锋等问题。实现高性能，高可用，可伸缩和最终一致性架构。是大型分布式系统不可缺少的中间件。

目前在生产环境，使用较多的消息队列有ActiveMQ，RabbitMQ，ZeroMQ，Kafka，MetaMQ，RocketMQ等。

关于各个组件的详细介绍和比较可看以下参考资料： 
>  
1. [java消息队列MQ](https://www.jianshu.com/p/ffd0806e0947)
2. [MQ框架的比较](https://www.cnblogs.com/ly-radiata/articles/5563744.html)  
3. [多维度对比5款主流分布式MQ消息队列，妈妈再也不担心我的技术选型了](https://cloud.tencent.com/developer/article/1449951)
4. [微服务中使用MQ——RabbitMQ](https://www.cnblogs.com/wangsen/p/11057714.html)

## RabbitMQ

初次学习MQ，文档丰富，社区活跃，易于上手搭建，有可视化界面是非常重要的，RabbitMQ对于上述要求做了很好的支持。 

我们这里暂不考虑性能，选择使用RabbitMQ编写一个demo。

[官网](https://www.rabbitmq.com/)

支持HTTP API: [RabbitMQ Management HTTP API](https://pulse.mozilla.org/api/)

### 原理
![原理图](../../src/image/20201123170216.png)
### 大致流程

1. consumer注册队列监听器到Broker（RabbitMQ）

Consumer首先注册一个队列监听器，来监听队列的状态，当队列状态变化时消费消息，
注册队列监听的时候需要提供：

- Exchange（交换器）信息：
交换类型(Dircet直连 ,Topic主题 ,Fanout广播)，交换器名称，是否自动删除等
- Queue（队列）信息，
名称，是否自动删除等
- 以及Routing Key（路由键）信息。
自定义的一个key值，这个值是连接Exchange和Queue的标识。

2. producer 发送消息到队列

producer 发送消息给RabbitMQ，需要在消息头中指定Exchange（交换器）信息，Routing Key（路由键）信息

3. Broker(RabbitMQ) 匹配
RebbitMQ通过Producer指定的Exchange名称找到交换器，然后通过指定的Routing key找到对应的队列，将消息放入队列中。
队列状态发生变化，Consumer就会通过监听器得到消息并消费。


> 什么时候使用消息队列呢？

关注下游执行执行结果，用RPC/REST,不关注下游执行结果，用MQ，不用RPC/REST.

对于需要强事务保证而且延迟敏感的，RPC是优于消息队列的。

比如：
你的服务器一秒能处理100个订单，但秒杀活动1秒进来1000个订单，持续10秒，在后端能力无法增加的情况下，
你可以用消息队列将总共10000个请求压在队列里，后台consumer按原有能力处理，100秒后处理完所有请求（而不是直接宕机丢失订单数据）。

> 注意：mq关心的是“通知”，而非“处理

简单的说：MQ只能保证消息按照顺序通知给consumer，不能保证consumer处理逻辑,比如：是不是按照顺序执行。

假设有三个消息： M1(发短信),M2（发邮件）,M3（站内推送）
在队列中的顺序为：M3,M2,M1 MQ能保证消息在消费的时候是按照这个顺序，
但是不能保证consumer，必须先发送站内推送，再发邮件，最后发短信，
因为这三个consumer接受到消息执行的业务时间很可能不相同的。

### Exchange
它的作用：用来接收生产者发送的消息并将这些消息路由给服务器中的队列。

Exchange是通过Routing Key来匹配对应的Queue的。

我们要知道在RabbitMQ中Exchange的类型以及Queue，还有Routing key都是由consumer端提供的，
producer只是提供Exchange和Routing key，broker根据producer提供的Exchange名字找到对应的交换器，然后再
根据路由键去匹配对应的队列，放入消息到队列中。

有好几种类型的Exchange：
- Direct类型的Exchange的Routing key就是全匹配。
- Topic类型的Exchange的Routing key就是部分匹配或者是模糊匹配。
- Fanout类型的Exchange的Routing key就是放弃匹配。达到广播的效果。
匹配肯定都是限制在同一个Exchange中的，也就是相同的Exchange进行匹配。
### 使用
创建`rabbitmq-provider`项目

导入依赖
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.amqp</groupId>
            <artifactId>spring-rabbit-test</artifactId>
            <scope>test</scope>
        </dependency>
```

配置文件
```
server:
  port: 8021
spring:
  application:
    name: rabbitmq-provider
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```

### Exchange为Direct

添加配置类`DirectRabbitConfig`

```java
@Configuration
public class DirectRabbitConfig {

    //队列 起名：TestDirectQueue
    @Bean
    public Queue TestDirectQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //   return new Queue("TestDirectQueue",true,true,false);

        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue("TestDirectQueue",true);
    }

    //Direct交换机 起名：TestDirectExchange
    @Bean
    DirectExchange TestDirectExchange() {
        //  return new DirectExchange("TestDirectExchange",true,true);
        return new DirectExchange("TestDirectExchange",true,false);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TestDirectRouting
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(TestDirectQueue()).to(TestDirectExchange()).with("TestDirectRouting");
    }

    @Bean
    DirectExchange lonelyDirectExchange() {
        return new DirectExchange("lonelyDirectExchange");
    }

}

```

添加控制器 `SendMessageController`

```java
@RestController
public class SendMessageController {

    //使用RabbitTemplate,这提供了接收/发送等等方法
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "test message, hello!";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String,Object> map=new HashMap<>();
        map.put("messageId",messageId);
        map.put("messageData",messageData);
        map.put("createTime",createTime);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend("TestDirectExchange", "TestDirectRouting", map);
        return "ok";
    }


}

```

创建`rabbitmq-customer`项目

导入依赖
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.amqp</groupId>
            <artifactId>spring-rabbit-test</artifactId>
            <scope>test</scope>
        </dependency>
    
```

配置文件

```
server:
  port: 8022
spring:
  application:
    name: rabbitmq-consumer
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```

添加配置类`DirectRabbitConfig`

```java
@Configuration
public class DirectRabbitConfig {

    //队列 起名：TestDirectQueue
    @Bean
    public Queue TestDirectQueue() {
        return new Queue("TestDirectQueue",true);
    }

    //Direct交换机 起名：TestDirectExchange
    @Bean
    DirectExchange TestDirectExchange() {
        return new DirectExchange("TestDirectExchange");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TestDirectRouting
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(TestDirectQueue()).to(TestDirectExchange()).with("TestDirectRouting");
    }
}

```

添加接收处理类 `DirectReceiver`
```java
@Component
@RabbitListener(queues = "TestDirectQueue")//监听的队列名称 TestDirectQueue
public class DirectReceiver {

    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("DirectReceiver消费者收到消息  : " + testMessage.toString());
    }

}
```

测试：
provider发送请求可使用 `rabbitmq-test.http`测试

customer接收消息会打印控制台

### Exchange为Topic

Exchange为Topic和Direct总体上是差不多的，区别是Exchange的匹配方式。

provider端新建配置
```java
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
```
controller在新增2个接口
```java
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
```


consumer端新建2个接收类
TopicReceiver1
```java
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
```

TopicReceiver2
```java
@Component
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(value = "topicQueue"),
                exchange = @Exchange(value = "topicExchange", type = ExchangeTypes.TOPIC),
                key = "topic*"
        )
)
public class TopicReceiver2 {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("2  收到topic消息：" + logMessage.toString());
    }

}
```
consumer接收如图
```
1  收到topic消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 17:38:51 CST 2020, userId=0)
2  收到topic消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 17:38:54 CST 2020, userId=0)
1  收到topic消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 17:39:12 CST 2020, userId=0)
2  收到topic消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 17:39:14 CST 2020, userId=0)
```

### Exchange为Fanout

provider新建类

```java
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
```
接口
```java
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

```

consumer添加3个接收类
```java
@Component
@RabbitListener(queues = "queueA")
public class FanoutReceiverA {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("A  收到queueA消息：" + logMessage.toString());
    }

}

@Component
@RabbitListener(queues = "queueB")
public class FanoutReceiverB {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("B  收到queueB消息：" + logMessage.toString());
    }

}

@Component
@RabbitListener(queues = "queueC")
public class FanoutReceiverC {

    @RabbitHandler
    public void process(LogMessage logMessage) {
        System.out.println("C  收到queueC消息：" + logMessage.toString());
    }

}
```

测试2次消息后，接收如下
```
A  收到queueA消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:14 CST 2020, userId=0)
B  收到queueB消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:14 CST 2020, userId=0)
C  收到queueC消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:14 CST 2020, userId=0)
A  收到queueA消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:18 CST 2020, userId=0)
C  收到queueC消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:18 CST 2020, userId=0)
B  收到queueB消息：LogMessage(id=0, msg=topic message, hello!, logLevel=error, serviceType=server provider, createTime=Mon Nov 23 16:55:18 CST 2020, userId=0)
```


### HTTP API
[文档](https://pulse.mozilla.org/api/)

postman测试：
```
http://guest:guest@127.0.0.1:15672/api/vhosts
```
返回：
```json
[
    {
        "cluster_state": {
            "rabbit@77e3d481bbe0": "running"
        },
        "description": "Default virtual host",
        "message_stats": {
            "ack": 41,
            "ack_details": {
                "rate": 0.0
            },
            "confirm": 1,
            "confirm_details": {
                "rate": 0.0
            },
            "deliver": 41,
            "deliver_details": {
                "rate": 0.0
            },
            "deliver_get": 41,
            "deliver_get_details": {
                "rate": 0.0
            },
            "deliver_no_ack": 0,
            "deliver_no_ack_details": {
                "rate": 0.0
            },
            "drop_unroutable": 0,
            "drop_unroutable_details": {
                "rate": 0.0
            },
            "get": 0,
            "get_details": {
                "rate": 0.0
            },
            "get_empty": 0,
            "get_empty_details": {
                "rate": 0.0
            },
            "get_no_ack": 0,
            "get_no_ack_details": {
                "rate": 0.0
            },
            "publish": 42,
            "publish_details": {
                "rate": 0.0
            },
            "redeliver": 0,
            "redeliver_details": {
                "rate": 0.0
            },
            "return_unroutable": 1,
            "return_unroutable_details": {
                "rate": 0.0
            }
        },
        "messages": 0,
        "messages_details": {
            "rate": 0.0
        },
        "messages_ready": 0,
        "messages_ready_details": {
            "rate": 0.0
        },
        "messages_unacknowledged": 0,
        "messages_unacknowledged_details": {
            "rate": 0.0
        },
        "metadata": {
            "description": "Default virtual host",
            "tags": []
        },
        "name": "/",
        "recv_oct": 18209,
        "recv_oct_details": {
            "rate": 0.0
        },
        "send_oct": 18754,
        "send_oct_details": {
            "rate": 0.0
        },
        "tags": [],
        "tracing": false
    }
]
```