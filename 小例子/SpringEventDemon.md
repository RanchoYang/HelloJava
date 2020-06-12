## SpringEventDemon

```java
package com.demon;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

public class OrderEvent extends ApplicationEvent {
    /**
     * 
     */
    private static final long serialVersionUID = 1407194944114061807L;

    private long orderId;

    public OrderEvent(Object source, long orderId) {
        super(source);
        this.orderId = orderId;
    }

    public void printOrderId() {
        System.out.println("OrderId::" + orderId);
    }
}

@Component
class Orderlistener implements ApplicationListener<OrderEvent> {
    @Override
    public void onApplicationEvent(OrderEvent event) {
        System.out.println("===========监听==============");
        event.printOrderId();
    }
}

@Component
class OrderAnnotationListener {
    
    @EventListener
    @Order(2)
    // @Async
    public void receiveEvent(OrderEvent event) throws InterruptedException {
        System.out.println("=========注解实现监听========");
        Thread.sleep(5000);
        event.printOrderId();
    }
}

@Component
class OrderAnnotatedListener {
    
    @EventListener
    @Order(1)
    // @Async
    public void receiveEvent(OrderEvent event) throws InterruptedException {
        System.out.println("=========注解实现的第一个监听========");
        Thread.sleep(5000);
        event.printOrderId();
    }
}

@RestController
class OrderPrintController{
    
    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(method = RequestMethod.GET, value = "printOrder")
    public String printOrder() {
        long orderId = new Random().nextLong();
        applicationContext.publishEvent(new OrderEvent(this, orderId));
        return "success";
    }
}
```

#### 总结：

1. 可以通过实现ApplicationListener来添加监听，也可以通过注解@EventListener实现
2. 如果加上注解@Order，可以实现排序
3. 如果加上注解@Async，可以实现异步，但是排序将失效



