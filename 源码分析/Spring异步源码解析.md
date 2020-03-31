## Spring异步源码解析

##### 一、AsyncConfigurationSelector

1. 在启动类EnableAsync里面，会导入类AsyncConfigurationSelector

2. AsyncConfigurationSelector实现了ImportSelector接口，具体作用是根据EnableAsync类里面的AdviceMode来做不同逻辑处理。

   ```java
   switch (adviceMode) {
   			case PROXY:
   				return new String[] {ProxyAsyncConfiguration.class.getName()};
   			case ASPECTJ:
   				return new String[] {ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME};
   			default:
   				return null;
   		}
   ```

3. 本篇根据默认的PROXY模式继续分析

##### 二、ProxyAsyncConfiguration

```java
AsyncAnnotationBeanPostProcessor bpp = new AsyncAnnotationBeanPostProcessor();
	bpp.configure(this.executor, this.exceptionHandler);
	Class<? extends Annotation> customAsyncAnnotation = this.enableAsync.getClass("annotation");
	if (customAsyncAnnotation != AnnotationUtils.getDefaultValue(EnableAsync.class, "annotation")) {
		bpp.setAsyncAnnotationType(customAsyncAnnotation);
	}
	bpp.setProxyTargetClass(this.enableAsync.getBoolean("proxyTargetClass"));
	bpp.setOrder(this.enableAsync.<Integer>getNumber("order"));
```
1. 创建一个AsyncAnnotationBeanPostProcessor实例
2. 如果实现了AsyncConfigurer接口并定义了executor和exceptionHandler的方法，将会在这里注入AsyncAnnotationBeanPostProcessor实例
3. 将enableAsync注解里面定义的属性注入实例（annotation,proxyTargetClass,order）

##### 三、AsyncAnnotationBeanPostProcessor

1. 间接实现了BeanFactoryAware和BeanPostProcessor接口

2. 在setBeanFactory里面，根据传入的executor和exceptionHandler创了一个AsyncAnnotationAdvisor，并将自定义的异步注解注入，目的是创建aop切入点

   ```java
   AsyncAnnotationAdvisor advisor = new AsyncAnnotationAdvisor(this.executor, this.exceptionHandler);
   		if (this.asyncAnnotationType != null) {
   			advisor.setAsyncAnnotationType(this.asyncAnnotationType);
   		}
   ```

3. AsyncAnnotationAdvisor的构造方法里面，分别创建了通知和切入点

   ```java
   this.advice = buildAdvice(executor, exceptionHandler);
   this.pointcut = buildPointcut(asyncAnnotationTypes);
   ```

4. 在buildAdvice方法里面，创建了AnnotationAsyncExecutionInterceptor实例

5. AnnotationAsyncExecutionInterceptor间接实现了MethodInterceptor接口，查看其invoke方法，用于开启新线程执行异步任务，并处理了异常情况

##### 四、JdkDynamicAopProxy

1. 继续回到AsyncAnnotationBeanPostProcessor这个类，找到其父类的postProcessAfterInitialization方法，在这个方法里面会创建代理对象

2. 创建JdkDynamicAopProxy

   ```java
   if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
   			Class<?> targetClass = config.getTargetClass();
   			if (targetClass == null) {
   				throw new AopConfigException("TargetSource cannot determine target class: " +
   						"Either an interface or a target is required for proxy creation.");
   			}
   			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
   				return new JdkDynamicAopProxy(config);
   			}
   			return new ObjenesisCglibAopProxy(config);
   		}
   		else {
   			return new JdkDynamicAopProxy(config);
   		}
   ```

3. JdkDynamicAopProxy执行invoke方法，最终调用到MethodInterceptor的invoke方法

   ```java
   // We need to create a method invocation...
   				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
   				// Proceed to the joinpoint through the interceptor chain.
   				retVal = invocation.proceed();
   ```

   


