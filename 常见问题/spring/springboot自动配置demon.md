# springboot自动配置demon

##### 一、读者须知

使用springboot集成其他插件的时候，非常的方便，只需要引入包并添加相应配置文件就可以使用了，这一强大的功能当然离不开springboot的自动配置，今天写了一个demon来自定义springboot的starter。starter里面简单包装了一个rocketmq的生产者，暂且不吐槽这个功能有什么用，先把具体过程记下来。

##### 二、具体实现

1. 首先创建一个maven项目，项目名为`mq-spring-boot-starter`，引入以下依赖：

   ```java
   <dependencies>
   		<dependency>
   			<groupId>org.springframework.boot</groupId>
   			<artifactId>spring-boot-autoconfigure</artifactId>
   			<version>2.1.5.RELEASE</version>
   		</dependency>
   		<dependency>
   			<groupId>org.apache.rocketmq</groupId>
   			<artifactId>rocketmq-client</artifactId>
   			<version>4.3.0</version>
   		</dependency>
   	</dependencies>
   ```

2. 在该项目下新建一个配置类`MQProperties`，里面会定义mq需要的配置。注意：这里设置了默认前缀`mq`

   ```java
   @ConfigurationProperties(prefix = MQProperties.DEFAULT_PREFIX)
   public class MQProperties {
   
   public static final String DEFAULT_PREFIX = "mq";
   private String producerGroup;
   private String namesrvAddr;
   
   public String getProducerGroup() {
   	return producerGroup;
   }
   public void setProducerGroup(String producerGroup) {
   	this.producerGroup = producerGroup;
   }
   public String getNamesrvAddr() {
   	return namesrvAddr;
   }
   public void setNamesrvAddr(String namesrvAddr) {
   	this.namesrvAddr = namesrvAddr;
   }
   
   }
   ```

3. mq生产者的实现类`RocketMQProducer`

   ```java
   public class RocketMQProducer implements InitializingBean{
   
   private DefaultMQProducer producer;
   
   private MQProperties properties;
   
   public RocketMQProducer(MQProperties properties) {
   	this.properties = properties;
   }
   
   private DefaultMQProducer initProducer() throws MQClientException {
   	producer = new DefaultMQProducer();
   	producer.setProducerGroup(properties.getProducerGroup());
   	producer.setNamesrvAddr(properties.getNamesrvAddr());
   	producer.setRetryTimesWhenSendFailed(3);
   	producer.start();
   	return producer;
   }
   
   public SendResult sendMessage(Message msg)
   		throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
   	if (producer == null) {
   		producer = initProducer();
   	}
   	SendResult result = producer.send(msg);
   	return result;
   }
   
   public void afterPropertiesSet() throws Exception {
   	// TODO Auto-generated method stub
   }
   
   }
   ```

4. 以上具体功能的实现和需要加载的配置都可以根据自己需求更改

5. 新建一个包`com.mq`并编写自动配置类`MQAutoConfiguraction`，这个类将会被springboot在启动时扫描并加入spring容器中

   ```java
   @Configuration
   @ConditionalOnClass(RocketMQProducer.class)
   @EnableConfigurationProperties(MQProperties.class)
   public class MQAutoConfiguraction {
   
   @Autowired
   private MQProperties properties;
   
   @Bean
   @ConditionalOnMissingBean
   @ConditionalOnProperty(prefix = "mq", value = "enabled", havingValue = "true")
   public RocketMQProducer getProducer() {
   	return new RocketMQProducer(properties);
   }
   
   }
   ```

6. 在resources下增加`META-INF`，并增加`spring.factories`文件，这一步是让springboot能扫描并加载指定的类，内容如下

   ```java
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.mq.MQAutoConfiguraction
   ```

7. 现在已经完成一个能够自动配置的starter了，使用这个starter也很简单，只需要在pom文件里面增加依赖，以及增加相应配置文件就好了

   ```java
    <dependency>
   			<groupId>com.mq</groupId>
   			<artifactId>mq-spring-boot-starter</artifactId>
   			<version>0.0.1-SNAPSHOT</version>
   		</dependency>
   ```

   ```java
   mq:
     enabled: true
     producerGroup: test
     namesrvAddr: 127.0.0.1:9876
   ```

##### 三、其他补充

1. starter命名：springboot官方的starter会以spring-boot-starter-xxx的格式命名，如spring-boot-starter-web；外部增加的starter则已xxx-spring-boot-starter的格式命名，如dubbo-spring-boot-starter

2. spring.factories文件的命名与路径：springboot在启动时候会加载配置文件，这个路径定义在SpringFactoriesLoader

   ```java
   public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
   ```

3. 