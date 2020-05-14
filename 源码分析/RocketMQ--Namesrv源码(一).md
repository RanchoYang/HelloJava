## RocketMQ--Namesrv源码(一)

##### 一、namesrv的启动，调用NamesrvStartup的main()方法

```java
 public static void main(String[] args) {
        main0(args);
    }
```

##### 二、创建NamesrvController

1. 设置系统属性--mq版本

2. 初始化命令选项：h-help;n-namesrvAddr

   ```java
   Options options = ServerUtil.buildCommandlineOptions(new Options());
   ```

3. 解析命令行参数

   ```java
   commandLine = ServerUtil.parseCmdLine("mqnamesrv", args, buildCommandlineOptions(options), new PosixParser());
   ```

4. 通过反射把命令行里的参数注入namesrvConfig和nettyServerConfig，通过MixAll.properties2Object()方法实现

5. 实例化NamesrvController

   ```java
   final NamesrvController controller = new NamesrvController(namesrvConfig, nettyServerConfig);
   ```

6. 合并配置

   ```java
   controller.getConfiguration().registerConfig(properties);
   ```

##### 三、启动controller

1. 调用initialize()

   1. 加载kv配置

   2. 实例化NettyRemotingServer

      ```java
      this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);
      ```

   3. 创建nettysever的线程池处理请求？

      ```java
      this.remotingExecutor =
                  Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactoryImpl("RemotingExecutorThread_"));
      ```

   4. 注册nettyserver的处理器，会实例化DefaultRequestProcessor用来处理remoting服务的各个请求

      ```java
      this.registerProcessor();
      ```

   5. 设置定时任务清理过期的broker

   6. 设置定时任务打印kv配置信息？？

      ```java
      NamesrvController.this.kvConfigManager.printAllPeriodically();
      ```

   7. 如果tlsMode不是disabled模式，则创建监听线程，查看证书或者私钥是否改变，更新认证信息

2. 初始化失败，则退出程序

3. 设置shutdown的钩子方法--调用controller的shutdown()

   ```java
   public void shutdown() {
           this.remotingServer.shutdown();
           this.remotingExecutor.shutdown();
           this.scheduledExecutorService.shutdown();   
           if (this.fileWatchService != null) {
               this.fileWatchService.shutdown();
           }
   }
   ```

4. 调用start()方法

   ```java
   public void start() throws Exception {
      this.remotingServer.start();   
      if (this.fileWatchService != null) {
           this.fileWatchService.start();
       }
   }
   ```
   1. 启动netty server
   2. 启动证书的监听线程开始监听

##### 四、打印serializeType

```java
String tip = "The Name Server boot success. serializeType=" + RemotingCommand.getSerializeTypeConfigInThisServer();
            log.info(tip);
```

