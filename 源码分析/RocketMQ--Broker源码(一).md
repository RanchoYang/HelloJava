## RocketMQ--Broker源码(一)

#### 一、BrokerStartup.main()

1. broker的启动类，首先创建一个broker的controller，再启动

#### 二、createBrokerController()

1. 将命令参数转为命令行

2. 创建borkerconfig对象，创建netty server和clinet config对象，创建message store config对象

3. 将命令行的参数注入几个config对象

4. 获取namesrv的地址列表，并创建socket

   ```java
   String[] addrArray = namesrvAddr.split(";");
                       for (String addr : addrArray) {
                           RemotingUtil.string2SocketAddress(addr);
                       }
   ```

5. 根据broker的角色，分别设置brokerId(master角色默认为0)

   ```java
   switch (messageStoreConfig.getBrokerRole()) {
                   case ASYNC_MASTER:
                   case SYNC_MASTER:
                       brokerConfig.setBrokerId(MixAll.MASTER_ID);
                       break;
                   case SLAVE:
                       if (brokerConfig.getBrokerId() <= 0) {
                           System.out.printf("Slave's brokerId must be > 0");
                           System.exit(-3);
                       }
                       break;
               default:
                   break;
           }
   ```

6. 打印配置

7. 创建BrokerController实例

   ```java
   final BrokerController controller = new BrokerController(
                   brokerConfig,
                   nettyServerConfig,
                   nettyClientConfig,
                   messageStoreConfig);
   ```

8. 合并各个配置

   ```java
   controller.getConfiguration().registerConfig(properties);
   ```

9. 初始化controller

   ```java
   boolean initResult = controller.initialize();
   ```

   1. 加载topic,consumerOffset,subscriptionGroup,consumerFilter的配置

      ```java
         boolean result = this.topicConfigManager.load();
         result = result && this.consumerOffsetManager.load();
         result = result && this.subscriptionGroupManager.load();
         result = result && this.consumerFilterManager.load();
      ```

   2. 加载成功，则创建DefaultMessageStore对象等

      ```java
      this.messageStore =
                          new DefaultMessageStore(this.messageStoreConfig, this.brokerStatsManager, this.messageArrivingListener,
                              this.brokerConfig);
      ```

   3. 创建netty server

      ```java
      this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.clientHousekeepingService);
      ```

   4. 创建fastRemotingServer？？

      ```java
      this.fastRemotingServer = new NettyRemotingServer(fastConfig, this.clientHousekeepingService);
      ```

   5. 创建线程池发送消息

   6. 创建线程池拉取消息

   7. 创建线程池查询消息

   8. 创建线程池获取borker线程池数？

   9. 创建线程池client管理器

   10. 创建线程池获取broker心跳

   11. 创建线程池consumer管理器

   12. 注册processor

       ```java
       this.registerProcessor();
       ```

       1. SendMessageProcessor
       2. PullMessageProcessor
       3. QueryMessageProcessor
       4. ClientManageProcessor
       5. ConsumerManageProcessor
       6. EndTransactionProcessor
       7. AdminBrokerProcessor

   13. 创建broker定时统计任务

       ```java
       BrokerController.this.getBrokerStats().record();
       ```

   14. 创建定时任务更新consumer offset

       ```java
       BrokerController.this.consumerOffsetManager.persist();
       ```

   15. 创建定时任务更新consumer filter

       ```java
       BrokerController.this.consumerFilterManager.persist();
       ```

   16. 创建定时任务关闭不活跃的broker?

       ```java
       BrokerController.this.protectBroker();
       ```

   17. 创建定时任务打印慢的线程队列（send,pull,query三个线程池）

       ```java
       BrokerController.this.printWaterMark();
       ```

   18. BrokerController.this.getMessageStore().dispatchBehindBytes()

   19. 创建定时任务获取namesrv地址并更新

   20. 判断该broker是否为slave角色，如果是则更新master的地址信息，定时同步topic,consumerOffset,delayOffset,subscriptionGroupConfig；否则定时打印master和slave之前差异

       ```java
       if (BrokerRole.SLAVE == this.messageStoreConfig.getBrokerRole()) {
           ...
       	BrokerController.this.slaveSynchronize.syncAll();
           ...
       }else{
           ...
       	BrokerController.this.printMasterAndSlaveDiff();
           ...
       }
       ```

   21. 如果tlsmode不是DISABLED，定时更新证书信息等

   22. initialTransaction()

       1. 实例化TransactionalMessageService
       2. 实例化TransactionalMessageCheckListener
       3. 实例化TransactionalMessageCheckService

10. 如果初始化controller失败，则调用shutdown()方法并退出

11. 添加shutdown的钩子方法

#### 三、start()

1. 调用brokerController的start()方法

   1. 调用messageStore，remotingServer，fastRemotingServer，fileWatchService，brokerOuterAPI，pullRequestHoldService，clientHousekeepingService，filterServerManager的start()方法

   2. 在namesrv注册所有broker

      ```java
      this.registerBrokerAll(true, false, true);
      ```

   3. 定期执行registerBrokerAll()方法

      ```java
      BrokerController.this.registerBrokerAll(true, false, brokerConfig.isForceRegister());
      ```

   4. 调用brokerStatsManager和brokerFastFailure的start()方法

   5. 如果broker的角色不是slave，则调用transactionalMessageCheckService的start()方法

2. 打印启动成功信息

