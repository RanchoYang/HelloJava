#### 一、ThreadPoolExecutor总的流程

#### 二、ThreadPoolExecutor属性

1. ctl：同时表示有效线程数(workerCount)和运行状态(runState)

   ```java
   private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
   ```

   初始二进制值：1010 XXXX ....

2. COUNT_BITS：用来计算各种状态

   ```
   private static final int COUNT_BITS = Integer.SIZE - 3;
   ```

   初始值：32-3 = 29

3. CAPACITY：表示最大容量

   ```
   private static final int CAPACITY   = (1 << COUNT_BITS) - 1;
   ```

   初始二进制值：0001 1111 1111 XXXX ....

4. RUNNING：能够接收新任务，以及对已添加的任务进行处理。

   ```
   private static final int RUNNING    = -1 << COUNT_BITS;
   ```

   初始二进制值：1010 0000 XXXX....

5. SHUTDOWN：不接收新任务，但能处理已添加的任务。 

   ```
   private static final int SHUTDOWN   =  0 << COUNT_BITS;
   ```

   初始二进制值：0000 0000 XXXX....

6. STOP：不接收新任务，不处理已添加的任务，并且会中断正在处理的任务。

   ```
   private static final int STOP       =  1 << COUNT_BITS;
   ```

   初始二进制值：0010 0000 XXXX....

7. TIDYING：当所有的任务已终止，ctl记录的”任务数量”为0。

   ```
   private static final int TIDYING    =  2 << COUNT_BITS;
   ```

   初始二进制值：0100 0000 XXXX....

8. TERMINATED ：线程池彻底终止

   ```
   private static final int TERMINATED =  3 << COUNT_BITS;
   ```

   初始二进制值：1000 0000 XXXX....

9. workQueue：用来存放任务的队列

   ```
   private final BlockingQueue<Runnable> workQueue;
   ```

10.  mainLock 

    ```
    private final ReentrantLock mainLock = new ReentrantLock();
    ```

11. workers 

    ```
    private final HashSet<Worker> workers = new HashSet<Worker>();
    ```

12.  termination 

    ```
    private final Condition termination = mainLock.newCondition();
    ```

13. largestPoolSize：

    ```
    private int largestPoolSize;
    ```

14. completedTaskCount

    ```
    private long completedTaskCount;
    ```

15. threadFactory

    ```
    private volatile ThreadFactory threadFactory;
    ```

16.  handler

    ```
    private volatile RejectedExecutionHandler handler;
    ```

17. keepAliveTime：除核心线程外，空闲线程存活时间

    ```
    private volatile long keepAliveTime;
    ```

18. allowCoreThreadTimeOut：默认为false，如果设置为true，则核心线程在timeout后也会停止

    ```
    private volatile boolean allowCoreThreadTimeOut;
    ```

19. corePoolSize：线程池允许的核心线程数量

    ```
    private volatile int corePoolSize;
    ```

20. maximumPoolSize：线程池允许的最大线程数量

    ```
    private volatile int maximumPoolSize;
    ```

21. defaultHandler：默认拒绝策略

    ```
    private static final RejectedExecutionHandler defaultHandler =
            new AbortPolicy();
    ```

22. shutdownPerm 

    ```
    private static final RuntimePermission shutdownPerm =
            new RuntimePermission("modifyThread");
    ```

23. acc

    ```
    private final AccessControlContext acc;
    ```


#### 三、一般方法介绍

1.  得到线程池运行状态

   ```
   private static int runStateOf(int c)     { return c & ~CAPACITY; }
   ```

2. 线程池存在的worker数量

   ```
   private static int workerCountOf(int c)  { return c & CAPACITY; }
   ```

3. 得到当前线程池的ctl数值

   ```
   private static int ctlOf(int rs, int wc) { return rs | wc; }
   ```

4. 比较当前ctl的值是否小于某个状态的值

   ```
   private static boolean runStateLessThan(int c, int s) {
           return c < s;
       }
   ```

5. 比较当前ctl的值是否大于/等于某个状态的值

   ```
   private static boolean runStateAtLeast(int c, int s) {
           return c >= s;
       }
   ```

6. 根据ctl数值判断线程池是否为运行状态

   ```
   private static boolean isRunning(int c) {
           return c < SHUTDOWN;
       }
   ```

7. 当前ctl值增加1

   ```
   private boolean compareAndIncrementWorkerCount(int expect) {
           return ctl.compareAndSet(expect, expect + 1);
       }
   ```

8. 当前ctl值减去1

   ```
   private boolean compareAndDecrementWorkerCount(int expect) {
           return ctl.compareAndSet(expect, expect - 1);
       }
   ```

9. 

   ```
   private void decrementWorkerCount() {
           do {} while (! compareAndDecrementWorkerCount(ctl.get()));
       }
   ```

#### 四、Worker介绍

**下面是内部类Worker源码**：

```java
private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        private static final long serialVersionUID = 6138294804551838833L;
		
        final Thread thread;
      
        Runnable firstTask;
        
        volatile long completedTasks;

		//拿到threadFactory对象，创建新线程
        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }
   
        public void run() {
        	//运行worker
            runWorker(this);
        }
    
    	//判断是否被锁住: 1=locked; 0=unlocked
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }
    
    	//当前线程上锁
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
    
    	//释放当前线程
        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }
    
    	//调用父类的acquire方法
        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        //调用父类的release方法
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }
    
    	//打断当前线程
        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()){
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```

**下面是acquire源码：**

```
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```

**下面是release源码：**

```
 public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
```

**下面是runWorker源码：**

```
final void runWorker(Worker w) {
		//拿到当前线程
        Thread wt = Thread.currentThread();
        //查看是否有任务加入当前线程，如果是新开启的线程，则有firstTask;如果线程早就开启，这里应该没有firstTask
        Runnable task = w.firstTask;
        //将firstTask清空
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
        	//1.如果新开启的线程，则task不为空；2.如果是已经开启的线程，则从队列里面去找
            while (task != null || (task = getTask()) != null) {
            //锁住当前线程
                w.lock();
               
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
```
**下面是getTask源码：**

```java
private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
        	//获取当前ctl值
            int c = ctl.get();
            //获取当前线程池状态
            int rs = runStateOf(c);
    
    		//判断线程池状态和队列状态，决定是否清除所有worker
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }
    
    		//得到当前worker数量
            int wc = workerCountOf(c);
    
            // 当timed为true(即worker数量大于核心线程数量或者设置了允许核心数量超时)的时候，表示该worker是可以销毁的
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
    
    		//如果worker数量大于最大线程数量或者超时，则worker数量减去1
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }
    
            try {
            	//
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }
```

#### 五、execute()方法介绍

**下面是execute源码：**

```java
 public void execute(Runnable command) {
 		//如果没有传入参数，直接返回NPE
        if (command == null)
            throw new NullPointerException();
        //得到当前的ctl值
        int c = ctl.get();
        //如果当前线程数量小于核心线程数量，就添加一个新的worker
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        //到这一步表示当前线程总数已经达到核心线程数量，新的任务会尝试加入队列
        //如线程池是运行状态，并且新任务加入队列成功（队列没有满）
        if (isRunning(c) && workQueue.offer(command)) {
       		//重新拿到当前ctl值
            int recheck = ctl.get();
            //如果此时线程池不再是运行状态，并且成功从队列移除任务成功，则根据拒绝策略处理该任务
            if (! isRunning(recheck) && remove(command))
                reject(command);
            //如果当前worker数量为0（可能调用shutdown方法后），则创建新的worker
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        //如果当前线程池不是运行状态或者队列已经满了，则根据拒绝策略处理该任务
    	//如果添加新worker（非核心线程）成功，则通过新线程来执行
        else if (!addWorker(command, false))
            reject(command);
    }
```

**下面是addWorker源码：**

```java
 private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
        	//获取当前ctl值
            int c = ctl.get();
            //获取当前线程池状态
            int rs = runStateOf(c);

            //如果线程池处于SHUTDOWN、STOP、TIDYING、TERMINATED状态则不再创建新的worker
            //注意:如果firstTask为空并且队列不为空，表示调用了shutdown方法，需要创建一个worker来执行队列里的任务
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;
    
            for (;;) {
            	//以下情况不创建新worker: 1.worker数量大于线程池运行的容量
            	//2.如果是创建核心线程，worker数量大（等）于核心线程数量
            	//3.如果是创建非核心线程，worker数量大（等）于最大线程数量
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }
    
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                //锁住当前线程
                mainLock.lock();
                try {
                   	//获取当前线程池状态
                    int rs = runStateOf(ctl.get());
    				//以下状态将会把worker加入worers，并且重新设置largestPoolSize
                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) 
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                	//开始线程，执行任务
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
```

