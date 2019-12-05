# mysql入门sql

收集了平时用得较多的sql

1. 登录mysql

   ```mysql
   mysql -uroot -p
   ```

2. 查询所有数据库

   ```mysql
   show databases
   ```

3. 使用某个数据库，order库为例

   ```mysql
   use order
   ```

4. 创建表user：id主键自增，status默认为1，create在插入数据时自动更新，modify在更新数据时自动更新

   ```mysql
    CREATE TABLE `user` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(32) NOT NULL COMMENT '名字',
     `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态',
     `create` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `modify` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
   )
   ```

5. 更改主键自增初始值

   ```mysql
   alter table user auto_increment = 10000
   ```

6. 给某个列增加普通索引，name列为例

   ```mysql
   ALTER TABLE `user` 
   ADD INDEX `name` (`name` ASC)
   ```

7. 给多个列增加联合索引，name和status列为例

   ```mysql
   ALTER TABLE `user` 
   ADD INDEX `name_status` (`name` ASC, 'status' ASC)
   ```

8. 在某列后增加某列，name列后增加address列，varchar类型

   ```mysql
   ALTER TABLE `user` 
   ADD COLUMN `address` VARCHAR(64) NULL AFTER `name`
   ```

9. 修改某列

   ```mysql
   ALTER TABLE `user` 
   CHANGE COLUMN `address` `addr` VARCHAR(60) NULL DEFAULT NULL
   ```

10. 删除某列

    ```mysql
    ALTER TABLE `user` 
    DROP COLUMN `address
    ```

11. 显示某个表的所有列，包括备注，描述等，user表为例

    ```mysql
    show full columns from user
    ```

12. 新插入一条数据

    ```mysql
    insert into user (name, status) values ('张三', 2)
    ```

13. 修改数据 

    ```mysql
    update user set name = '李四', status = 1 where id = 1
    ```

14. 删除数据 

    ```mysql
    delete from user where id = 1
    ```

15. 关闭数据库安全模式：在修改和删除某数据时候，如果条件不是主键，会报错[You are using safe update mode and you tried to update a table without a WHERE that uses a KEY column...]

    ```mysql
    SET SQL_SAFE_UPDATES = 0
    ```

16. 开启安全模式

    ```mysql
    SET SQL_SAFE_UPDATES = 1
    ```

17. 查看数据库当前安全模式

    ```mysql
    show variables like 'SQL_SAFE_UPDATES'
    ```

18. 简单查询

    1. 查询名字是`张三`的数据

       ```mysql
       select * from user where name = '张三'
       ```

    2. 查询名字是`张三`并且状态是`1`的数据

       ```mysql
       select * from user where name = '张三' and status = 1
       ```

    3. 查询名字是`张三`或者是`李四`的数据

       ```mysql
       select * from user where name = '张三' or name = '李四'
       ```

    4. ...

19. 模糊查询

    1. 查询名字包含`张三`的数据

       ```mysql
       select * from user where name like '%张三%'
       ```

    2. 查询名字以`张`开始的字段

       ```mysql
       select * from user where name like '张%'
       ```

    3. 查询名字以`三`结尾的数据

       ```mysql
       select * from user where name like '%三'
       ```

20. 子查询

21. 连接查询