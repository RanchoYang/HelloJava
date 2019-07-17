#### MD5加密字符串：

```java
public static void main(String[] args)throws Exception {
    	//待加密字符串
    	String password = "123";
    	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(password.getBytes());
        //摘要字节数组
        byte[] digest = messageDigest.digest();
        //把二进制的散列值转为十六进制
        char[] hexs = new char[]{'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuffer newPassword = new StringBuffer();
        for (byte by : digest) {
            newPassword.append(hexs[(by >> 4) & 15]);
            newPassword.append(hexs[by & 15]);
        }
        //输出转为十六进制的加密密码
        System.out.println(newPassword.toString());
    }
```

注意：

1. 一般采用加盐方式减少密码碰撞几率

#### MD5加密文件：

未完待续

