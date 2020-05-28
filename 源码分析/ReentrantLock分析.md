## ReentrantLock分析

#### 一、ReentrantLock简述

1. ReentrantLock的lock和unlock方法实际上是调用内部类Sync的lock和unlock方法，Sync继承了AbstractQueuedSynchronizer，有两个实现类分别实现了公平锁和非公平锁
2. 下文分析非公平锁源码

#### 二、lock()

1. 首先尝试cas获取锁，如果获取成功则设置锁被当前线程独有

   ```java
   if (compareAndSetState(0, 1))
       setExclusiveOwnerThread(Thread.currentThread());
   ```

2. 如果未获取到锁，则调用acquire()方法

   1. 调用nonfairTryAcquire()获取锁

      1. 如果此时锁已经刚好被释放了，则通过cas获取锁

      2. 如果此时锁仍然是被占有的，则查看占有锁的线程是不是就是当前线程（可重入锁）

         ```java
         else if (current == getExclusiveOwnerThread()) {
             int nextc = c + acquires;
             if (nextc < 0) // overflow
                 throw new Error("Maximum lock count exceeded");
             setState(nextc);
             return true;
         }
         ```

      3. 如果占用锁的线程和当前线程是同一个线程，则设置state加1，否则返回false

   2. 当前线程未获取到锁，则加入等待队列

      1. 创建一个新的节点，设置节点的线程为当前线程

         ```java
         Node node = new Node(Thread.currentThread(), mode);
         ```

      2. 获取队列尾部节点，如果tail节点不为空则将该节点设置为tail节点的下一个节点

      3. 如果tail节点为空，则证明该节点为队列的第一个节点，先创建一个空的头节点

         ```java
         Node t = tail;
         if (t == null) { // Must initialize
             if (compareAndSetHead(new Node()))
                 tail = head;
         }
         ```

      4. 通过自旋加入队列（并发情况下，存在其他线程抢占队列前面位置？？）

   3. 调用acquireQueued()

      1. 如果当前节点的前继节点为头节点，则尝试获取锁，获取成功则将自己设置为头节点

         ```java
         final Node p = node.predecessor();
         if (p == head && tryAcquire(arg)) {
             setHead(node);
             p.next = null; // help GC
             failed = false;
             return interrupted;
         }
         ```

      2. 调用shouldParkAfterFailedAcquire()查看是否需要park当前线程，调整队列

      3. 如果需要park当前线程，则调用parkAndCheckInterrupt()

      4. 如果失败，则调用cancelAcquire()

         ```java
         finally {
             if (failed)
                 cancelAcquire(node);
         }
         ```

#### 三、shouldParkAfterFailedAcquire()

1. 如果前继节点为SIGNAL状态，则当前线程需要park，返回true

2. 如果前继节点状态为取消状态，则将前继节点的前继节点设置为当前节点的前继节点，直至找到状态不是取消状态的前继节点，返回false

   ```java
   if (ws > 0) {
       /*
        * Predecessor was cancelled. Skip over predecessors and
        * indicate retry.
        */
       do {
           node.prev = pred = pred.prev;
       } while (pred.waitStatus > 0);
       pred.next = node;
   } 
   ```

3. 如果前继节点状态不是SIGNAL和CANCELED，则设置前继节点状态为SIGNAL（自旋第二次进入时候，会将当前线程设置为需要park，参考第一步）

   ```java
   compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
   ```

#### 四、cancelAcquire()

1. 如果当前节点为null，则直接返回

2. 设置当前节点的线程为null

   ```java
   node.thread = null;
   ```

3. 查找当前节点的前继节点，如果前继节点状态已经取消，则把前继节点的前继节点设置为当前节点的前继节点，重复该步骤

   ```java
   Node pred = node.prev;
   while (pred.waitStatus > 0)
       node.prev = pred = pred.prev;
   ```

4. 设置当前节点为取消状态

   ```java
   node.waitStatus = Node.CANCELLED;
   ```

5. 如果当前节点为队列的tail节点，则设置当前节点的前继节点为tail，设置前继节点的后继节点为null

   ```java
   if (node == tail && compareAndSetTail(node, pred)) {
   	compareAndSetNext(pred, predNext, null);
   }
   ```

6. 如果当前节点的前继节点不是head并且前继节点状态为SIGNAL并且前继节点的线程不为null，则设置前继节点的后继节点为当前节点的后继节点

   ```java
    if (pred != head &&
        ((ws = pred.waitStatus) == Node.SIGNAL ||
         (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
        pred.thread != null) {
        Node next = node.next;
        if (next != null && next.waitStatus <= 0)
            compareAndSetNext(pred, predNext, next);
    }
   ```

7. 如果不满足第6步条件，则调用unparkSuccessor()唤醒后继节点

   ```java
   else {
       unparkSuccessor(node);
   }
   ```

#### 五、unlock()

1. tryRelease()

   1. 检查要释放锁的线程是不是就是当前线程

      ```java
      if (Thread.currentThread() != getExclusiveOwnerThread())
          throw new IllegalMonitorStateException();
      ```

   2. state减一，再查看state是否已经为0，如果为0则释放锁成功

      ```java
      int c = getState() - releases;
      ...
      if (c == 0) {
          free = true;
          setExclusiveOwnerThread(null);
      }
      ```

   3. 如果state不为0（同一线程多次获取过锁），则更新state并返回false

2. 如果释放锁成功，则查看队列的头节点，如果head存在并且状态不为初始状态，则调用unparkSuccessor()

   ```java
   if (h != null && h.waitStatus != 0)
       unparkSuccessor(h);
   ```

#### 六、unparkSuccessor()

1. 设当前节点状态为0

2. 如果当前节点的下一个节点不存在或者状态为取消状态，则从队列里从尾往头找到头节点的下一个状态不为取消的节点

   ```java
   Node s = node.next;
   if (s == null || s.waitStatus > 0) {
       s = null;
       for (Node t = tail; t != null && t != node; t = t.prev)
           if (t.waitStatus <= 0)
               s = t;
   }
   ```

3. 如果当前节点的下一个节点存在且状态不是取消状态，unpark当前节点的下一个节点

   ```java
   LockSupport.unpark(s.thread);
   ```