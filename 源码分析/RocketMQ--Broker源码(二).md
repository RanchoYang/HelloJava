## RocketMQ--Broker源码(二)

#### 一、SendMessageProcessor处理请求逻辑分析

#### 二、processRequest()

1. 如果requestCode是CONSUMER_SEND_MSG_BACK，则调用consumerSendMsgBack()

2. 解析并创建request header，如果没有获取到header信息，则返回null

3. buildMsgContext()
   
   1. 判断是否有sendMessageHook，如果没有则返回null
   
      ```java
      if (!this.hasSendMessageHook()) {
                  return null;
              }
      ```
   
   2. 创建SendMessageContext实例并设置对应属性(producerGroup,topic,broker等)
   
   3. 解析request header，并将uniqueKey注入SendMessageContext实例
   
      ```java
      String uniqueKey = properties.get(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
      ```
   
   4. 给request header的properties增加两个新的属性：MSG_REGION和TRACE_ON
   
4. executeSendMessageHookBefore()

   1. 遍历所有钩子方法分别执行

5. sendMessage()或者sendBatchMessage()，根据header里面的标识判断消息是否为批量消息，分别处理

   ```java
   if (requestHeader.isBatch()) {
                       response = this.sendBatchMessage(ctx, request, mqtraceContext, requestHeader);
                   } else {
                       response = this.sendMessage(ctx, request, mqtraceContext, requestHeader);
                   }
   ```

6. executeSendMessageHookAfter()

   1. 遍历所有钩子方法分别执行

#### 三、sendMessage()

1. 创建response

   ```java
   final RemotingCommand response = RemotingCommand.createResponseCommand(SendMessageResponseHeader.class);
   ```

2. 判断在当前时间broker是否能接受发送消息请求

   ```java
   if (this.brokerController.getMessageStore().now() < startTimstamp) {
               response.setCode(ResponseCode.SYSTEM_ERROR);
               response.setRemark(String.format("broker unable to service, until %s", UtilAll.timeMillisToHumanString2(startTimstamp)));
               return response;
           }
   ```

3. msgCheck()

4. 根据topic获取topic相关config

   ```java
   TopicConfig topicConfig = this.brokerController.getTopicConfigManager().selectTopicConfig(requestHeader.getTopic());
   ```

5. 创建MessageExtBrokerInner对象

6. handleRetryAndDLQ()，死信队列？

   ```java
   if (!handleRetryAndDLQ(requestHeader, response, request, msgInner, topicConfig)) {
               return response;
           }
   ```

   1. 重新设置msgInner对象的topic和queueId属性
   2. 设置msgInner对象的SysFlag属性

7. 映射msgInner属性

8. 判断该条消息是不是事物消息，如果是，判断该broker是否拒绝处理事物消息,是则返回错误码

   ```java
   String traFlag = oriProps.get(MessageConst.PROPERTY_TRANSACTION_PREPARED);
           if (traFlag != null && Boolean.parseBoolean(traFlag)) {
               if (this.brokerController.getBrokerConfig().isRejectTransactionMessage()) {
                   response.setCode(ResponseCode.NO_PERMISSION);
                   response.setRemark(
                       "the broker[" + this.brokerController.getBrokerConfig().getBrokerIP1()
                           + "] sending transaction message is forbidden");
                   return response;
               }
               putMessageResult = this.brokerController.getTransactionalMessageService().prepareMessage(msgInner);
           } else {
               putMessageResult = this.brokerController.getMessageStore().putMessage(msgInner);
           }
   ```

9. handlePutMessageResult()
   1. 根据message status设置response code

   2. 如果消息发送成功，更新broker统计数据

      ```java
      this.brokerController.getBrokerStatsManager().incTopicPutNums(msg.getTopic(), putMessageResult.getAppendMessageResult().getMsgNum(), 1);
                  this.brokerController.getBrokerStatsManager().incTopicPutSize(msg.getTopic(),
                      putMessageResult.getAppendMessageResult().getWroteBytes());
                  this.brokerController.getBrokerStatsManager().incBrokerPutNums(putMessageResult.getAppendMessageResult().getMsgNum());
      ```

   3. 设置response header信息

   4. 处理response

      ```java
      doResponse(ctx, request, response);
      ```

   5. 查看是否有钩子方法，如果有则更新SendMessageContext实例

#### 四、msgCheck()

1. 如果该broker不能写数据并且该topic是ordertopic，则返回错误码NO_PERMISSION

2. 查看该topic名字是否和系统默认topic名冲突，返回错误码SYSTEM_ERROR

3. 根据topic获取相关配置信息

4. 如果没有找到对应配置信息，首先设置topicSysFlag，创建一个新的topicConfig

   ```java
   topicConfig = this.brokerController.getTopicConfigManager().createTopicInSendMessageMethod
   ```

5. 校验queueId是否合法，如果该id大于等于该topic的写或读队列数，则返回错误码SYSTEM_ERROR

#### 五、putMessage()

1. 判断message store的状态，如果是shutdown则禁止putMessage

2. 判断broker角色是否为slave，如果是则禁止putMessage

3. 判断该store 是否可以写入数据，如果不允许则禁止putMessage

4. 判断topic的长度

5. 判断msg的properties长度？？

   ```java
   if (msg.getPropertiesString() != null && msg.getPropertiesString().length() > Short.MAX_VALUE) {
               log.warn("putMessage message properties length too long " + msg.getPropertiesString().length());
               return new PutMessageResult(PutMessageStatus.PROPERTIES_SIZE_EXCEEDED, null);
           }
   ```

6. isOSPageCacheBusy

   ```java
   if (this.isOSPageCacheBusy()) {
               return new PutMessageResult(PutMessageStatus.OS_PAGECACHE_BUSY, null);
           }
   ```

7. 调用commitLog的putMessage()

   ```java
   PutMessageResult result = this.commitLog.putMessage(msg);
   ```

8. 更新发送消息记录

#### 六、commitLog.putMessage()

1. 获取storeStatsService

   ```java
   StoreStatsService storeStatsService = this.defaultMessageStore.getStoreStatsService();
   ```

2. 获取message的tranType，如果是TRANSACTION_NOT_TYPE或者TRANSACTION_COMMIT_TYPE，则重新设置延迟topic和queueId

3. 获取最后一个MappedFile

   ```java
   MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile();
   ```

4. 如果获取到的MappedFile已经满了或者没有获取到，则获取第一个MappedFile；如果还是没有获取到，则报错CREATE_MAPEDFILE_FAILED

   ```java
   if (null == mappedFile || mappedFile.isFull()) {
                   mappedFile = this.mappedFileQueue.getLastMappedFile(0); // Mark: NewFile may be cause noise
               }
               if (null == mappedFile) {
                   log.error("create mapped file1 error, topic: " + msg.getTopic() + " clientAddr: " + msg.getBornHostString());
                   beginTimeInLock = 0;
                   return new PutMessageResult(PutMessageStatus.CREATE_MAPEDFILE_FAILED, null);
               }
   ```

5. 调用mappedFile的appendMessage()方法写入消息

   ```java
   result = mappedFile.appendMessage(msg, this.appendMessageCallback);
   ```

6. 如果上一步返回的状态为END_OF_FILE，则重新创建一个mappedFile并写入

7. 创建返回对象PutMessageResult

   ```java
   PutMessageResult putMessageResult = new PutMessageResult(PutMessageStatus.PUT_OK, result);
   ```

8. handleDiskFlush()

9. handleHA()

#### 七、appendMessage()

1. 计算当前写数据位置currentPos

   ```java
   int currentPos = this.wrotePosition.get();
   ```

2. 如果当前位置大于mappedFile的fileSize，则报错

3. 获取ByteBuffer实例，并设置position

   ```java
   ByteBuffer byteBuffer = writeBuffer != null ? writeBuffer.slice() : this.mappedByteBuffer.slice();
               byteBuffer.position(currentPos);
   ```

4. 调用doAppend()

   1. 计算wroteoffset

      ```java
      long wroteOffset = fileFromOffset + byteBuffer.position();
      ```

   2. reset hostHolder

      ```java
      this.resetByteBuffer(hostHolder, 8);
      ```

   3. 创建msgId

      ```java
      String msgId = MessageDecoder.createMessageId(this.msgIdMemory, msgInner.getStoreHostBytes(hostHolder), wroteOffset);
      ```

   4. 根据topic和queueId创建key，用来保存queueOffset

   5. 通过key从queueTable查询queueOffset，如果找不到则设置queueOffset=0并加入queueTable

      ```java
      Long queueOffset = CommitLog.this.topicQueueTable.get(key);
                  if (null == queueOffset) {
                      queueOffset = 0L;
                      CommitLog.this.topicQueueTable.put(key, queueOffset);
                  }
      ```

   6. 如果消息的tranType是TRANSACTION_PREPARED_TYPE或者TRANSACTION_ROLLBACK_TYPE，设置queueOffset=0

   7. 判断properties长度是否过长

   8. 计算msg长度并判断是否过长

   9. 判断mappedFile剩余空间是否足够：如果不足，则首先reset msgStoreItemMemory，映射到当前的queue bytebuffer，创建result对象，status=END_OF_FILE并返回

   10. 初始化storage space，依次存入信息：totalsize,magiccode,bodycrc,queueId,flag,queueoffset,physicaloffset,sysflag,borntimestamp,bornhost,bornHost,storeHost,reconsumeTimes,preparedTransactionOffset,body,topic,properties

   11. 把storage里面的信息映射到当前的queue bytebuffer，创建result对象

       ```java
       byteBuffer.put(this.msgStoreItemMemory.array(), 0, msgLen);
       ```

   12. 如果tranType是TRANSACTION_NOT_TYPE或者TRANSACTION_COMMIT_TYPE，则topicQueueTable里面的queueOffset增加1

   13. 返回result

5. 更新写数据的位置，更新store时间

   ```java
   this.wrotePosition.addAndGet(result.getWroteBytes());
   this.storeTimestamp = result.getStoreTimestamp();
   ```

#### 八、handleDiskFlush()

1. CommitLog在初始化的時候，会分别创建两刷盘实例flushCommitLogService，commitLogService

   ```java
   if (FlushDiskType.SYNC_FLUSH == defaultMessageStore.getMessageStoreConfig().getFlushDiskType()) {
           this.flushCommitLogService = new GroupCommitService();
       } else {
           this.flushCommitLogService = new FlushRealTimeService();
       }
   this.commitLogService = new CommitRealTimeService();
   ```

2. Synchronization flush

   1. 获取GroupCommitService实例，提交新的request到requestsWrite列表等待刷盘
   2. GroupCommitService里面有个线程，将requestsWrite复制到requestsRead列表，再调用doCommit刷盘，刷盘完成则清除requestsRead列表
   3. 等待刷盘结果
   4. 如果失败，则返回status=FLUSH_DISK_TIMEOUT

3. Asynchronous flush

   1. FlushRealTimeService.wakeup()
   2. CommitRealTimeService.wakeup()

4. FlushRealTimeService.run()

   1. 获取提交数据间隔和上次提交时间与当前时间做比较，如果小于当前时间则设置最小提交页面为0，设置flush process
   2. mappedFileQueue.flush()
      1. 根据offset找到对应的mappedFile
      2. mappedFile.flush()
   3. service shutdown 重试

5. CommitRealTimeService.run()

   1. 获取提交数据间隔和上次提交时间与当前时间做比较，如果小于当前时间则设置最小提交页面为0
   2. 调用mappedFileQueue的commit()方法
      1. 通过offset找出mappedFile
      2. mappedFile.commit()，提交数据到channel？
   3. 如果提交结果为false(部分数据已经提交)，则调用wakeup()
   4. waitForRunning()
   5. service shutdown 重试

   



