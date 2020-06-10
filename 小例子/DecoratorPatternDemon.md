## DecoratorPatternDemon

```java
package com.demon;

public class DecoratorPatternDemon {
    
    public static void main(String[] args) {
        Worker worker = new PeopleDecorator(new People("Tom"));
        worker.work();
    }
}

interface Worker {
	void work();
}

class People implements Worker {
    
    private String name;

    public String getName() {
        return name;
    }

    public People(String name) {
        this.name = name;
    }

    @Override
    public void work() {
        System.out.println(this.name + " start work...");
    }
}

class PeopleDecorator implements Worker {
    
    private People worker;

    public PeopleDecorator(People worker) {
        this.worker = worker;
    }

    @Override
    public void work() {
        drink();
        worker.work();
    }

    private void drink() {
        System.out.println(worker.getName() + " start drink...");
    }
}
```


	


	

