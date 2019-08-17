## 一、RocketMQ下载安装

1. 进入官网，下载需要的版本：http://rocketmq.apache.org/dowloading/releases/

   我下载的4.3版本，[**http://mirror.bit.edu.cn/apache/rocketmq/4.3.0/rocketmq-all-4.3.0-bin-release.zip**](http://mirror.bit.edu.cn/apache/rocketmq/4.3.0/rocketmq-all-4.3.0-bin-release.zip)

2. 解压，并配置环境变量：ROCKETMQ_HOME，加入path

3. 分别启动nameserver 和 broker

   ```
   start mqnamesrv.cmd
   ```

   ```
   start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true
   ```

4. 运行jps命令查看是否启动成功

## 二、控制台下载安装

1. 使用git命令下载控制台源码：

   ```
   $ git clone -b release-rocketmq-console-1.0.0 https://github.com/apache/rocketmq-externals.git
   ```

2. 可以自己修改配置文件，这里先定义好prot和namesrvAddr等

   ```
   rocketmq.config.namesrvAddr=127.0.0.1:9876
   ```

3. 打包，进入项目，使用maven命令：

   ```
   mvn clean package -Dmaven.test.skip=true
   ```

4. 运行生成的jar包，命令如下：

   ```
   java -jar xxxxx.jar
   ```

## 三、java实现生产者和消费者

从官网扣下来的几个demo:

1. SyncProducer ：

   ```java
   public class SyncProducer {
       public static void main(String[] args) throws Exception {
           //Instantiate with a producer group name.
           DefaultMQProducer producer = new
               DefaultMQProducer("please_rename_unique_group_name");
           // Specify name server addresses.
           producer.setNamesrvAddr("localhost:9876");
           //Launch the instance.
           producer.start();
           for (int i = 0; i < 100; i++) {
               //Create a message instance, specifying topic, tag and message body.
               Message msg = new Message("TopicTest" /* Topic */,
                   "TagA" /* Tag */,
                   ("Hello RocketMQ " +
                       i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
               );
               //Call send message to deliver message to one of brokers.
               SendResult sendResult = producer.send(msg);
               System.out.printf("%s%n", sendResult);
           }
           //Shut down once the producer instance is not longer in use.
           producer.shutdown();
       }
   }
   ```

   

2. AsyncProducer ：

   ```java
   public class AsyncProducer {
       public static void main(String[] args) throws Exception {
           //Instantiate with a producer group name.
           DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
           // Specify name server addresses.
           producer.setNamesrvAddr("localhost:9876");
           //Launch the instance.
           producer.start();
           producer.setRetryTimesWhenSendAsyncFailed(0);
           for (int i = 0; i < 100; i++) {
                   final int index = i;
                   //Create a message instance, specifying topic, tag and message body.
                   Message msg = new Message("TopicTest",
                       "TagA",
                       "OrderID188",
                       "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
                   producer.send(msg, new SendCallback() {
                       @Override
                       public void onSuccess(SendResult sendResult) {
                           System.out.printf("%-10d OK %s %n", index,
                               sendResult.getMsgId());
                       }
                       @Override
                       public void onException(Throwable e) {
                           System.out.printf("%-10d Exception %s %n", index, e);
                           e.printStackTrace();
                       }
                   });
           }
           //Shut down once the producer instance is not longer in use.
           producer.shutdown();
       }
   }
   ```

   

3. Consumer ：
```java
   public class Consumer {

   
   public static void main(String[] args) throws InterruptedException, MQClientException {
   
       // Instantiate with specified consumer group name.
       DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name");
        
       // Specify name server addresses.
       consumer.setNamesrvAddr("localhost:9876");
       
       // Subscribe one more more topics to consume.
       consumer.subscribe("TopicTest", "*");
       // Register callback to execute on arrival of messages fetched from brokers.
       consumer.registerMessageListener(new MessageListenerConcurrently() {
   
           @Override
           public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
               ConsumeConcurrentlyContext context) {
               System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
               return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
           }
       });
   
       //Launch the consumer instance.
       consumer.start();
   
       System.out.printf("Consumer Started.%n");
   }
   
   }
```

