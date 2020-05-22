```java
package com.demon;

import java.util.Calendar;

public class ListenerDemon {
    public static void main(String[] args) {
        UserProcessor userProcessor = new UserProcessor();
        userProcessor.registerUserListener(new UserListener() {
            int talkCount = 1;

            @Override
            public void work(UserEvent userEvent) {
                System.out.println(Calendar.getInstance().getTime() + "\n" + userEvent.getUser() + "开始工作");
            }

            @Override
            public void talk(UserEvent userEvent) {
                System.out.println(
                        Calendar.getInstance().getTime() + "\n" + userEvent.getUser() + "说话" + talkCount + "次");
                talkCount++;
            }
        }, "小王");

        new Thread(userProcessor).start();
        System.out.println("主线程完成。。");
    }
}

interface UserListener {
    void work(UserEvent userEvent);

    void talk(UserEvent userEvent);
}

class UserEvent {
    private String name;

    public UserEvent(String name) {
        this.name = name;
    }

    public String getUser() {
        return this.name;
    }
}

class UserProcessor implements Runnable {
    private UserListener userListener;

    private UserEvent userEvent;

    public void registerUserListener(UserListener userListener, String userName) {
        this.userListener = userListener;
        this.userEvent = new UserEvent(userName);
    }

    public void eat() {

    }

    public void work() {
        userListener.work(userEvent);
    }

    public void talk() {
        userListener.talk(userEvent);
    }

    @Override
    public void run() {
        work();
        eat();
        int count = 0;
        while (count < 3) {
            talk();
            count++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
```



