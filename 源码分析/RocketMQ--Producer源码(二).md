## RocketMQ--Producer源码(二)

##### 一、send(Message msg)

1. 默认同步发送消息，3秒超时

   ```java
   public SendResult send(Message msg,
           long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
           return this.sendDefaultImpl(msg, CommunicationMode.SYNC, null, timeout);
       }
   ```

##### 二、sendDefaultImpl()

1. 首先校验topic等是否符合

   ```java
    Validators.checkMessage(msg, this.defaultMQProducer);
   ```

2. 通过topic寻找对应的发布信息

   ```java
   TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
   ```

   1. 首先从发布信息列表查找该主题的发布信息，如果缓存里面存在则从缓存里获取

      ```java
      TopicPublishInfo topicPublishInfo = this.topicPublishInfoTable.get(topic);
      ```

   2. 没有合适的就创建一个新的发布信息并加入发布信息列表，更新主题的路由信息
      1. 加锁

      2. 从nameServer获取该主题的路由信息

         ```java
         topicRouteData = this.mQClientAPIImpl.getTopicRouteInfoFromNameServer(topic, 1000 * 3);
         ```

      3. 判断该主题的路由信息是否有变更，如果没有变更，则判断是需要变更（如果该主题没有发布信息记录或者之前发布的队列不存在，则需要变更路由信息）

         ```java
         boolean changed = topicRouteDataIsChange(old, topicRouteData);
                                 if (!changed) {
                                     changed = this.isNeedUpdateTopicRouteInfo(topic);
                                 } else {
                                     log.info("the topic[{}] route info changed, old[{}] ,new[{}]", topic, old, topicRouteData);
                                 }
         ```

      4. 如果需要变更路由信息，则先找出所有的broker并且更新，接着创建新的发布信息并加入路由信息，更新发布信息列表，更新订阅信息列表

   3. 如果此时该主题对应的发布信息包含路由信息或者状态是ok，就返回发布信息；否则返回默认的发布信息

      ```java
      if (topicPublishInfo.isHaveTopicRouterInfo() || topicPublishInfo.ok()) {
                  return topicPublishInfo;
              } else {
                  this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic, true, this.defaultMQProducer);
                  topicPublishInfo = this.topicPublishInfoTable.get(topic);
                  return topicPublishInfo;
              }
      ```

   4. 如果没有找到发布信息，则查看是否有nameServer，没有则报错没有nameserver地址；否则报错找不到该主题对应路由信息

3. 设置重试次数和已经重试次数

   ```java
   int timesTotal = communicationMode == CommunicationMode.SYNC ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
               int times = 0;
   ```

4. 查找一个broker和一个队列用于发送消息，如果是重试发送消息，则尝试寻找一个新的broker，如果有新的broker则使用新的，没有则继续使用之前的broker

   ```java
   MessageQueue mqSelected = this.selectOneMessageQueue(topicPublishInfo, lastBrokerName);
   ```

5. 调用sendKernelImpl()，返回发送结果

   ```java
   sendResult = this.sendKernelImpl(msg, mq, communicationMode, sendCallback, topicPublishInfo, timeout - costTime);
   ```

6. 更新错误信息记录列表

   ```java
   this.updateFaultItem(mq.getBrokerName(), endTimestamp - beginTimestampPrev, false);
   ```

7. 判断发送状态是否为成功，如果不是成功则判断是否使用其他broker重试，如果未设置使用其他broker重试，则返回结果

   ```java
   if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                                       if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
                                           continue;
                                       }
                                   }
   ```

8. 重复步骤4-7，如果重试次数等于已经重试次数或者超时，抛出异常

##### 三、sendKernelImpl()

1. 从broker地址列表获取broker地址

   ```java
   String brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
   ```

2. 如果找不到broker地址，则调用tryToFindTopicPublishInfo()方法，再一次尝试从namesrv获取地址

   ```java
   if (null == brokerAddr) {
               tryToFindTopicPublishInfo(mq.getTopic());
               brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
           }
   ```

3. 如果找不到broker地址，则报错broker不存在

4. 查看是否开启了vip通道，如果开启了则获取vip的broker地址

   ```java
   brokerAddr = MixAll.brokerVIPChannel(this.defaultMQProducer.isSendMessageWithVIPChannel(), brokerAddr);
   ```

5. 如果消息体不是批量消息，给该消息设置唯一id

6. 如果消息过大，则压缩消息体

7. 判断是否为事物消息

8. 执行发送消息前的钩子方法

9. 创建发送消息的头部信息，group,topic,queueId等

10. 调用api的sendMessage()

11. 执行发送消息后的钩子方法

##### 四、MQClientAPIImpl.sendMessage()

1. 判断是否发送smart消息或者该消息是批量消息，分别创建远程请求request，注意创建request时候传入的RequestCode，namesrv接收到消息后会通过code做不同的处理

   ```java
    if (sendSmartMsg || msg instanceof MessageBatch) {
               SendMessageRequestHeaderV2 requestHeaderV2 = SendMessageRequestHeaderV2.createSendMessageRequestHeaderV2(requestHeader);
               request = RemotingCommand.createRequestCommand(msg instanceof MessageBatch ? RequestCode.SEND_BATCH_MESSAGE : RequestCode.SEND_MESSAGE_V2, requestHeaderV2);
           } else {
               request = RemotingCommand.createRequestCommand(RequestCode.SEND_MESSAGE, requestHeader);
           }
   ```

2. sendMessageSync()

   1. remotingClient.invokeSync()
   2. 调用processSendResponse()处理发送结果

##### 五、invokeSync(String addr, final RemotingCommand request, long timeoutMillis)

1. 获取channel，实际上和namesrv通信

   ```java
   final Channel channel = this.getAndCreateChannel(addr);
   ```

2. 如果channel状态不可以，则关闭channel并抛出异常

3. 处理请求前的钩子方法

4. invokeSyncImpl()

   ```java
   RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis - costTime);
   ```

5. 处理请求后的钩子方法

##### 六、processSendResponse()

1. 设置response的状态码

2. 创建发送结果实例SendResult（属性包括消息队列号，主题，broker等），内容如下：

   > SendResult [sendStatus=SEND_OK, msgId=0A540495005E49C2FAAE40F972A40021, offsetMsgId=0A0D218B00002A9F0000000001DA77B6, messageQueue=MessageQueue [topic=my_topic, brokerName=broker-2, queueId=0], queueOffset=108]