## RocketMQ--PushConsumer源码(一)

一、start()

1. 服务默认状态为CREATE_JUST，更改为START_FAILED

2. 检查配置信息

   1. group名字合法性
   2. 是否设置消息模式
   3. 是否设置消费顺序
   4. 验证消费时间戳格式
   5. 是否设置分配队列的策略
   6. 是否订阅了消息
   7. 是否注册了监听以及注册的监听器是否正常
   8. 设置消费线程数合法性
   9. 校验拉取队列阈值合法性
   10. 校验拉主题阈值合法性
   11. 校验拉取消息间隔合法性
   12. 消费最大消息批次？？
   13. 拉取批次大小？？
   14. 

3. 复制订阅消息主题到rebalance的主题列表

   ```java
    this.copySubscription();
   ```

4. 如果消息模式为集群，则更改消费者实例名称

   ```java
   if (this.defaultMQPushConsumer.getMessageModel() == MessageModel.CLUSTERING) {
                       this.defaultMQPushConsumer.changeInstanceNameToPID();
                   }
   ```

5. 获取mQClientFactory实例

   ```java
   this.mQClientFactory = MQClientManager.getInstance().getAndCreateMQClientInstance(this.defaultMQPushConsumer, this.rpcHook);
   ```

6. 获取PullAPIWrapper实例

   ```java
   this.pullAPIWrapper = new PullAPIWrapper(
                       mQClientFactory,
                       this.defaultMQPushConsumer.getConsumerGroup(), isUnitMode());
                   this.pullAPIWrapper.registerFilterMessageHook(filterMessageHookList);
   ```

7. 获取或者创建offsetStore实例并load，用于读取储存的消息？

8. 根据监听器获取consumeMessageService实例并调用其start()方法

9. 向mQClientFactory注册消费者，如果注册失败，则更新状态为CREATE_JUST，并shutdown consumeMessageService

10. 调用mQClientFactory的start()方法

11. 更新状态为RUNNING

12. 如果订阅的消息有改变，则更新？

    ```java
    this.updateTopicSubscribeInfoWhenSubscriptionChanged();
    ```

13. 检查消费者在broker的状态

    ```java
    this.mQClientFactory.checkClientInBroker();
    ```

14. 向所有broker发送心跳

    ```java
    this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();
    ```

15. rebalanceService

    ```java
    this.mQClientFactory.rebalanceImmediately();
    ```

二、

