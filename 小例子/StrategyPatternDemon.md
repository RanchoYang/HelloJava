## StrategyPatternDemon

```java
package com.demon;

public class StrategyPatternDemon {
   
    public static void main(String[] args) {

        MessageCenter messageCenter1 = new MessageCenter(new BatchMessageSend());
        messageCenter1.send();

        MessageCenter messageCenter2 = new MessageCenter(new SingleMessageSend());
        messageCenter2.send();

        messageCenter2.setMessageSend(new BatchMessageSend());
        messageCenter2.send();
    }
}

interface MessageSend {
    void sendMsg(String msg);
}

class SingleMessageSend implements MessageSend {
    public void sendMsg(String msg) {
		System.out.println("单条短信：" + msg);
	}
}

class BatchMessageSend implements MessageSend {
    public void sendMsg(String msg) {
		System.out.println("多条短信：" + msg);
	}
}

class MessageCenter {
    
    private MessageSend messageSend;

    public MessageCenter(MessageSend messageSend) {
        this.messageSend = messageSend;
    }

    public void setMessageSend(MessageSend messageSend) {
        this.messageSend = messageSend;
    }

    public void send() {
        messageSend.sendMsg("hello world!");
    }
}
```



