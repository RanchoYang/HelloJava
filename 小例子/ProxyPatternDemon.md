## ProxyPatternDemon

```java
package com.demon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyPatternDemon {
    public static void main(String[] args) {
        Traveling traveling = (Traveling) new PassengerProxy(new Passenger()).getProxy();
        traveling.buyTicket();
    }
}

interface Traveling {
	void buyTicket();
}

class Passenger implements Traveling {
    @Override
    public void buyTicket() {
        System.out.println("buy a ticket...");
    }
}

class PassengerProxy implements InvocationHandler {

    private Object proxy;

    private Object instance;

    public Object getProxy() {
        return proxy;
    }

    public PassengerProxy(Traveling traveling) {
        this.instance = traveling;
        proxy = Proxy.newProxyInstance(traveling.getClass().getClassLoader(), traveling.getClass().getInterfaces(),
                this);
    }

    private void check() {
        System.out.println("proxy check user info before buy a ticket...");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        check();
        return method.invoke(instance, args);
    }
}
```

