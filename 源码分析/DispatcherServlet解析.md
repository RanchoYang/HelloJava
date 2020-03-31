## DispatcherServlet解析

#### 一、servlet的生命周期

1. init
   1. 在DispatcherServlet的父类HttpServletBean里面，有init方法的具体实现，校验了必需参数\
   2. 初始化WebApplicationContext实例，并将servletContext,config,namespace和监听等注入实例(spring启动时进行)
   3. 刷新WebApplicationContext
   4. 将handlerMapping,handlerAdapter等注入DispatcherServlet(有请求时候触发**一次**)
2. service
   1. 在DispatcherServlet的doService()中具体实现
3. destory
   1. 调用webApplicationContext的close()方法，发布shutdown事件，关闭bean工厂等

#### 二、doService

1. 备份request的属性
2. doDispatch()方法
   1. 检查请求是否为Multipart类型，如果是(content-type以`multipart/`开始)则转为该类型
   2. 调用getHandler()为当前请求寻找合适的处理器
   3. 获取HandlerAdapter
   4. 调用拦截器的PreHandle()方法
   5. 通过handlerAdapter调用handler()方法
   6. 查看modelAndView是否需要使用默认名字
   7. 调用拦截器的PostHandle()方法
   8. 处理dispatch结果

#### 三、getHandler()

1. 遍历servlet所有的handlerMappings，去匹配适合当次请求的handler

   1. 在AbstractHandlerMethodMapping的afterPropertiesSet()方法里面，初始化了handlerMethod

   2.  判断候选类是否为处理器的方法isHandler(beanType)实现如下

      ```java
      protected boolean isHandler(Class<?> beanType) {
      		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
      				AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
      	}
      ```

   3. 扫描所有包含了Controller或者RequestMapping注解的类，并注册该类里面的各个处理方法(requestMapping标注的方法)

2. 获取该请求的URI去匹配handler，通过匹配方法，参数，头部信息等获取匹配度最高的handler

3. 如果匹配不到合适的handler,则采用默认的handler

4. 创建handlerExecutionChain，将拦截器注入该实例

5. getHandlerAdapter()方法去获取适合的适配器--RequestMappingHandlerAdapter

#### 四、拦截器preHandle()方法

1. 遍历所有拦截器，分别调用preHandle()
2. 如果上一步返回了false，则执行triggerAfterCompletion()
3. 从后往前遍历所有拦截器，执行afterCompletion()方法

#### 五、handle()

1. 调用RequestMappingHandlerAdapter.handleInternal()

2. 首先检查是否支持处理这个请求的http method，然后再检查是否需要session

3. 调用invokeHandlerMethod()方法

4. 创建invocableMethod实例

5. 创建ModelAndViewContainer，并把attributes加入model里面

6. 调用invokeAndHandle()方法

   ```java
   invocableMethod.invokeAndHandle(webRequest, mavContainer);
   ```

7. 返回modelAndView

#### 六、invokeAndHandle()

1. invokeForRequest()

   1. 解析验证请求参数
   2. 通过反射调用请求的方法

2. setResponseStatus()

3. handleReturnValue()

   1. 选择合适的handler处理返回值，这里选择分析RequestResponseBodyMethodProcessor，这个处理器可以处理ResponseBody注解标注的返回值

      ```java
      @Override
      	public boolean supportsReturnType(MethodParameter returnType) {
      		return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
      				returnType.hasMethodAnnotation(ResponseBody.class));
      	}
      ```

   2. createInputMessage()

   3. createOutputMessage()

   4. writeWithMessageConverters()

      1. isResourceType()
      2. 选择MediaType，通过请求的类型和可以接受的类型做匹配，选择最优
      3. 选择合适的converter
      4. 转换body信息并写出去

#### 七、拦截器PostHandle()方法

1. 获取所有拦截器执行postHandle()方法

#### 八、处理dispatch结果

1. 设置modelAndView名字
2. 解析处理结果(modelAndView或者exception)
3. 判断是否有异常，有异常则包装成处理异常的modelAndView
4. 渲染modelAndView

