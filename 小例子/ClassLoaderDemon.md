## ClassLoaderDemon

```java
package com.demon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ClassLoaderDemon extends ClassLoader {
    
    private File file;

    public ClassLoaderDemon(String path) {
        this.file = new File(path);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        FileChannel fc = null;
        try {
            fc = FileChannel.open(file.toPath());
            ByteBuffer bb = ByteBuffer.allocate((int) file.length());
            fc.read(bb);
            byte[] by = bb.array();
            return defineClass(name, by, 0, by.length);
        } catch (IOException e) {
            try {
                if (fc != null)
                    fc.close();
            } catch (IOException e1) {
            }
        }
        return super.findClass(name);
    }

    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("Test", true, new ClassLoaderDemon("E:/Test.class"));
        Object o = clazz.newInstance();
        System.out.println(o);
        for (Method method : o.getClass().getDeclaredMethods()) {
            if ("say".equals(method.getName())) {
                method.invoke(o, "小明");
            } else {
                method.invoke(o);
            }
        }
    }
}
```
下面是`Test`代码，将代码编译放入指定`E`文件夹用于测试：

```java
public class Test {
    public void say(String name) {
        System.out.println("I am "+ name);
    }

    public void work() {
        System.out.println("working!!!");
    }

    @Override
    public String toString() {
        return "hello world";
    }
}
```
