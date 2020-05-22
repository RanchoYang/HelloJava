```java
package com.demon;

import java.util.concurrent.CountDownLatch;

public class CountDownDemon {

    public static void main(String[] args) {
        System.out.println("开始做组队任务，三人任务都完成才算任务完成" );
        long startTime = System.currentTimeMillis();
        CountDownLatch countDownLatch  = new CountDownLatch(3);
        Task task1 = new Task(1000, "小王", countDownLatch);
        Task task2 = new Task(6000, "小明", countDownLatch);
        Task task3 = new Task(3000, "小强", countDownLatch);
        new Thread(task1).start();
        new Thread(task2).start();
        new Thread(task3).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        System.out.println("任务完成，花费"+(System.currentTimeMillis()-startTime)+"秒");
    }
}

class Task implements Runnable {
    private long time;
    private String user;
    private CountDownLatch countDownLatch;

    public Task(long time, String user, CountDownLatch countDownLatch) {
        this.time = time;
        this.user = user;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
            countDownLatch.countDown();
            System.out.println(user + "完成任务，花了" + time / 1000 + "秒");
        } catch (InterruptedException e) {
        }
    }
}
```
