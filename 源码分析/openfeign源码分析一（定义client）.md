## openfeign源码分析一（定义client）

1. #### EnableFeignClients

   1. 定义扫描的包，类以及配置文件等
   2. 注入FeignClientsRegistrar

2.  #### FeignClientsRegistrar

   1. 实现了ImportBeanDefinitionRegistrar，重写registerBeanDefinitions方法

      ```java
      @Override
      public void registerBeanDefinitions(AnnotationMetadata metadata,
            BeanDefinitionRegistry registry) {
         registerDefaultConfiguration(metadata, registry);
         registerFeignClients(metadata, registry);
      }
      ```

   2. registerDefaultConfiguration：如果在EnableFeignClients注解里面设置了配置类，则将其包装为FeignClientSpecification并注册，方便将配置类注入FeignContext

      ```java
      @Autowired(required = false)
      private List<FeignClientSpecification> configurations = new ArrayList<>();
      
      @Bean
      public FeignContext feignContext() {
          FeignContext context = new FeignContext();
          context.setConfigurations(this.configurations);
          return context;
      }
      ```

   3. registerFeignClients

      1. 确定要扫描的范围，根据EnableFeignClients注解里面value，basePackages，basePackageClasses和clients属性确定最终要扫描的包
      2. 在扫描范围内，添加了FeignClient注解的**接口**将会定义成feignclient
      3. 获取FeignClient注解上的属性，确定feignclient的名字
      4. 将FeignClient里面的配置类包装为FeignClientSpecification，并注册
      5. 根据FeignClient注解里面的属性注册client，包装成FeignClientFactoryBean
      
      

