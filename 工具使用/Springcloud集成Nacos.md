# Springcloud集成Nacos

##### 一、Nacos下载及安装

下载地址：https://nacos.io/en-us/

文档：https://nacos.io/en-us/docs/what-is-nacos.html

**注意：**下载完成后进入nacos的bin目录运行`startup.cmd`即可，如果出现数据库问题，可以按照如下方式将数据库改为mysql：

1. 下载mysql并安装

2. 创建nacos数据库，并且将nacos\conf目录下的nacos-mysql.sql导入数据库：建表，插入数据

3. 打开application.properties并找到`Config Module Related Configurations`部分，将其中mysql的注释放开

   ```java
   spring.datasource.platform=mysql
   
   db.num=1
   
   db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.user=root
   db.password=root
   ```

4. 注意mysql连接参数，用户和密码是否正确

5. 再次运行`startup.cmd`

6. 进入可视化界面：http://127.0.0.1:8848/nacos/index.html，账号密码默认是`nacos`

##### 二、springcloud集成nacos配置中心

1. nacos配置中心需要的依赖

   ```java
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
       <version>2.2.1.RELEASE</version>
   </dependency>
   ```

2. 在bootstrap.yml配置文件增加nacos配置

   ```java
   spring:
     profiles:
       active: dev
     cloud:
       nacos:
         config:
           server-addr: 127.0.0.1:8848
           group: demon
           file-extension: yaml
     application:
       name: demon
   ```

   注意：

   - 这里使用bootstrap文件是因为比application文件更早的被加载
   - spring.profiles.active，file-extension以及application.name三个属性将会组成nacos的data id，规则为${prefix}-${spring.profile.active}.${file-extension}
   - 如果在nacos后台配置文件的时候选择了group，在这里也需要配置对应的group，否则会读取不到nacos的配置文件

3. 在相应controller层增加`@RefreshScope`注解，可以动态更新配置文件

4. 进入nacos后台增加对应的配置文件，data id =demon-dev.yaml, group = demon, 文件类型为yaml，见第2点。如果多个环境则可以在nacos增加多个对应配置文件，如demon-dev.yaml，demon-qa.yaml，demon-prod.yaml，在bootstrap里面更改spring.profiles.active属性为dev，qa，prod即可。

##### 三、springcloud集成nacos服务注册中心

1. nacos服务注册和发现的依赖

   ```java
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
       <version>2.2.1.RELEASE</version>
   </dependency>
   ```

2. 在bootstrap.yml配置文件增加nacos配置

   ```java
   spring:
     profiles:
       active: dev
     cloud:
       nacos:
         config:
           server-addr: 127.0.0.1:8848
           group: demon
           file-extension: yaml
         discovery:
           group: dev
           server-addr: 127.0.0.1:8848
   
     application:
       name: demon
   ```

3. 在启动类增加`@EnableDiscoveryClient`注解

4. 此时服务提供者已经配置好了,启动服务即可在nacos后台`服务管理-服务列表`查看，以下步骤是消费者需要额外做的

5. 使用RestTemplate调用

   1. 在启动类注入restTemplate

      ```java
      @LoadBalanced
      @Bean
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
      ```

   2. 调用提供者

      ```java
      restTemplate.getForEntity("http://demon/login", String.class).getBody();
      ```

6. 使用Feign调用

   1. 增加Feign依赖

      ```java
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-openfeign</artifactId>
          <version>2.2.1.RELEASE</version>
      </dependency>
      ```

   2. 启动类增加`@EnableFeignClients`注解

   3. 调用提供者，需要注意name属性为服务提供者的名字

      ```java
      @FeignClient(name = "demon")
      public interface FeignProxy {
      
          @RequestMapping(value = "/login", method = RequestMethod.GET)
          String invoke();
      }
      ```

   4. 在其他类通过`@Autowired`注解注入`FeignProxy`即可

   

   

