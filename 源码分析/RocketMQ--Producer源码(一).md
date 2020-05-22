## RocketMQ--Producer源码(一)

#### 一、start()

1. 调用start()方法，默认入参为true

   ```java
   public void start() throws MQClientException {
           this.start(true);
       }
   ```

2. 判断服务状态，默认为CREATE_JUST状态，会修改其状态为START_FAILED

3. 检查group名称是否符合规范

   ```java
   this.checkConfig();
   ```

4. 如果group不是默认的group，则更改instanceName

   ```java
   if (!this.defaultMQProducer.getProducerGroup().equals(MixAll.CLIENT_INNER_PRODUCER_GROUP)) {
                       this.defaultMQProducer.changeInstanceNameToPID();
                   }
   ```

5. 从缓存获取生产者的MQClientInstance，如果没有就创建一个新的

   ```java
   this.mQClientFactory = MQClientManager.getInstance().getAndCreateMQClientInstance(this.defaultMQProducer, rpcHook);
   ```

6. 将生产者注册到MQClientInstance，如果该生产者group已经注册过，则将状态更改为CREATE_JUST并抛出异常

   ```java
   boolean registerOK = mQClientFactory.registerProducer(this.defaultMQProducer.getProducerGroup(), this);
   ```

7. 创建该topic的发布信息并存入发布信息集合

   ```java
   this.topicPublishInfoTable.put(this.defaultMQProducer.getCreateTopicKey(), new TopicPublishInfo());
   ```

8. 调用MQClientInstance的start方法

   ```java
   if (startFactory) {
                       mQClientFactory.start();
                   }
   ```

9. 更新服务状态为RUNNING

10. 向所有broker发送心跳

    ```java
    this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();
    ```

#### 二、MQClientInstance.start()

1. 如果nameserver地址为空，则重新获取并更新

   ```java
   if (null == this.clientConfig.getNamesrvAddr()) {
                           this.mQClientAPIImpl.fetchNameServerAddr();
                       }
   ```

2. 开启clientAPI通信

   ```java
   this.mQClientAPIImpl.start();
   ```

3. 开启一些定时任务
   1. 获取并更新nameServer的地址，用于和namesrv通信？

      ```java
      MQClientInstance.this.mQClientAPIImpl.fetchNameServerAddr();
      ```

   2. 更新topic的路由信息，拉取最新的消费者订阅和生产者发布的消息

      ```java
      MQClientInstance.this.updateTopicRouteInfoFromNameServer();
      ```

   3. 清除下线的Broker，并且向所有broker发送心跳

      ```java
      MQClientInstance.this.cleanOfflineBroker();
      MQClientInstance.this.sendHeartbeatToAllBrokerWithLock();
      ```

   4. persist消费者的offset

      ```java
      MQClientInstance.this.persistAllConsumerOffset();
      ```

   5. 调节线程池

      ```java
      MQClientInstance.this.adjustThreadPool();
      ```

4. pullMessageService.start()

5. rebalanceService.start();

6. 重新运行默认生产者的start方法，参数为false？？

   ```java
   this.defaultMQProducer.getDefaultMQProducerImpl().start(false);
   ```

7. 设置服务状态为RUNNING

   ```java
   this.serviceState = ServiceState.RUNNING;
   ```

三、

