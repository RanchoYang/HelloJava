# mysql索引测试

##### 一、写在最前面

说到sql优化，很多时候都会想到索引，但同时会有很多疑问：索引应该怎么用？是否加了索引就会生效？下面是一些测试索引的记录，测试时间为同一sql执行五次时间的评价值

##### 二、单表查询

1. 创建一个people表，建表语句如下

   ```
   CREATE TABLE `people` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(64) DEFAULT '',
     `code` varchar(64) DEFAULT '',
     PRIMARY KEY (`id`)
   )
   ```

2. 插入50万条数据，插入的规则：id从1开始递增，name列为People_id的格式，code列在id小于300000时为随机数字，从300000开始值固定为test

3. 如果查询条件为**name ='People_499999'**，则表示查询最后一条数据；如果查询条件为**name ='People_1'**，则表示查询第一条数据

   | sql语句                                                  | name是否加索引 |   时间（单位：ms）    |
   | :------------------------------------------------------- | :------------: | :-------------------: |
   | select * from people where name ='People_499999'         |       否       |         218.8         |
   | select * from people where name ='People_499999' limit 1 |       否       |         196.8         |
   | select * from people where name ='People_1'              |       否       |         203.2         |
   | select * from people where name ='People_1' limit 1      |       否       |           0           |
   | select * from people where name ='People_499999'         |       是       | 基本0ms，极少出现15ms |
   | select * from people where name ='People_499999' limit 1 |       是       | 基本0ms，极少出现15ms |
   | select * from people where name ='People_1'              |       是       |           0           |
   | select * from people where name ='People_1' limit 1      |       是       |           0           |

4. in和like的查询条件，name列加上了索引，code列没有加索引

   | sql语句                                                      | 时间（单位：ms） |
   | ------------------------------------------------------------ | :--------------: |
   | select * from people where code ='test' and name ='People_499998' |     基本0ms      |
   | select * from people where name in('People_499998')          |        0         |
   | select * from people where name like '%People_499998%'       |       250        |

5. 其他补充

##### 三、多表查询

1. 新增一个addr表，建表语句如下，p_id相当于外键，关联people表

   ```
   CREATE TABLE `addr` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `p_id` int(11) NOT NULL,
     `region` varchar(64) DEFAULT '',
     `address` varchar(64) DEFAULT '',
     PRIMARY KEY (`id`)
   )
   ```

2. 插入50万条数据，插入的规则：id从1开始递增，p_id也是从1开始递增，region列为Region_id的格式，address列为Addr_id的格式

3. people表name列加了索引的情况

   | sql语句                                                      | p_id是否加索引 | 时间（单位：ms） |
   | ------------------------------------------------------------ | :------------: | :--------------: |
   | select * from people p, addr a where p.id = a.p_id and p.code ='2027189214' ; |       否       |       753        |
   | select * from people p, addr a where p.id = a.p_id and p.name ='People_499999' |       否       |       197        |
   | select * from people p, addr a where p.id = a.p_id and p.code ='2027189214' |       是       |      728.6       |
   | select * from people p, addr a where p.id = a.p_id and p.name ='People_499999' |       是       |     基本0ms      |

4. 

##### 四、总结

1. 在查询条件没有加索引的情况下，如果查询单条数据，在sql语句最后加上limit效率会有提升，取决于被查询的数据在表中的位置
2. 查询条件单一且加了索引的情况下，sql语句最后加上limit 1与未加上时查询所花时间接近，查询第一条数据与查询最后一条数据所花时间也接近
3. in语句里索引依然有效，但是在like语句里，索引失效
4. 多表查询时，如果某个查询条件有索引，会提供查询效率；如果两张表关联的条件也加上索引，查询效率会更高