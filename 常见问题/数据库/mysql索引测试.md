# mysql索引测试

[TOC]

##### 一、写在最前面

说到sql优化，很多时候都会想到索引，但同时会有很多疑问：索引应该怎么用？是否加了索引就会生效？下面是一些测试索引的记录，测试时间为同一sql执行五次时间的平均值

##### 二、单表普通索引

1. 创建一个people表，建表语句如下

   ```java
   CREATE TABLE `people` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `name` varchar(64) DEFAULT '',
     `code` varchar(64) DEFAULT '',
     PRIMARY KEY (`id`)
   )
   ```

2. 插入50万条数据，插入的规则：id从1开始递增，name列为People_id的格式，code列在id小于300000时为随机数字，从300000开始值固定为test，如下：

   |   id   |     name      |    code    |
   | :----: | :-----------: | :--------: |
   | 299999 | People_299999 | 2027189214 |
   | 495545 | People_495545 |    test    |

3. 如果查询条件为`name ='People_499999'`，则表示查询最后一条数据；如果查询条件为`name ='People_1'`，则表示查询第一条数据，下面为没有索引时候的查询

   | sql语句                                                  | 时间（单位：ms） |
   | :------------------------------------------------------- | :--------------: |
   | select * from people where name ='People_499999'         |      218.8       |
   | select * from people where name ='People_499999' limit 1 |      196.8       |
   | select * from people where name ='People_1'              |      203.2       |
   | select * from people where name ='People_1' limit 1      |        0         |

4. name列加上普通索引

   | sql语句                                                      | 时间（单位：ms） |
   | ------------------------------------------------------------ | :--------------: |
   | select * from people where code ='test' and name ='People_499998' |        0         |
   | select * from people where name ='People_499999'             |        0         |
   | select * from people where name ='People_499999' limit 1     |        0         |
   | select * from people where name ='People_1'                  |        0         |
   | select * from people where name ='People_1' limit 1          |        0         |

5. 索引失效情况测试

   | sql语句                                                      | 是否有效 |
   | ------------------------------------------------------------ | :------: |
   | select * from people where name like '%People_499998%'       |    否    |
   | select * from people where name in('People_499998')          |    是    |
   | select * from people where name not in ( 'People_499998')    |    否    |
   | select * from people where name is null                      |    是    |
   | select * from people where name is not null                  |    否    |
   | select * from people where left(name,12) = 'People_49999'    |    否    |
   | select * from people where name != 'People_499998'           |    否    |
   | select * from people where name ='People_299996' or name ='People_299996' |    是    |
   | select * from addr where p_id > '499998'                     |    否    |
   | select * from addr where p_id between 1 and  499998          |    否    |
   | select * from people where name ='People_299999' or code = '2027189214' |    否    |

6. 

##### 三、多表普通索引

1. 新增一个addr表，建表语句如下，p_id相当于外键，关联people表

   ```java
   CREATE TABLE `addr` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `p_id` int(11) NOT NULL,
     `region` varchar(64) DEFAULT '',
     `address` varchar(64) DEFAULT '',
     PRIMARY KEY (`id`)
   )
   ```

2. 插入50万条数据，插入的规则：id从1开始递增，p_id也是从1开始递增，region列为Region_id的格式，address列为Addr_id的格式，如下：

   |  id  | p_id |  region  | address |
   | :--: | :--: | :------: | :-----: |
   |  5   |  5   | Region_5 | Addr_5  |

3. 以下查询都建立在people表name列加了索引的基础上

   | sql语句                                                      | p_id是否加索引 | 时间（单位：ms） |
   | ------------------------------------------------------------ | :------------: | :--------------: |
   | select * from people p, addr a where p.id = a.p_id and p.code ='2027189214' ; |       否       |       753        |
   | select * from people p, addr a where p.id = a.p_id and p.name ='People_499999' |       否       |       197        |
   | select * from people p, addr a where p.id = a.p_id and p.code ='2027189214' |       是       |      728.6       |
   | select * from people p, addr a where p.id = a.p_id and p.name ='People_499999' |       是       |        0         |

4. 

##### 四、单表联合索引

1. people表再增多一列phone，默认值为null

   ```java
   ALTER TABLE `people` 
   ADD COLUMN `phone` VARCHAR(20) NULL AFTER `code` 
   ```

2. 给其中`code=2027189214`的数据更新`phone=13177778888`用来测试，如下：

   |   id   |     name      |    code    |    phone    |
   | :----: | :-----------: | :--------: | :---------: |
   | 299999 | People_299999 | 2027189214 | 13177778888 |
   | 495545 | People_495545 |    test    |    null     |

3. 给name和code和phone列增加一个联合索引，顺序为name->code->phone

   | sql语句                                                      |       key        | ref   | 时间（单位：ms） |
   | ------------------------------------------------------------ | :--------------: | ----- | :--------------: |
   | select * from people where code = '2027189214'               |     联合索引     |       |      206.4       |
   | select * from people where phone = '13177778888'             |     联合索引     |       |      202.8       |
   | select * from people where code = '2027189214' limit 1       |     联合索引     |       |      103.2       |
   | select * from people where name = 'People_299999' and code='2027189214' | name列的普通索引 | const |        0         |
   | select * from people where name = 'People_299999'            | name列的普通索引 | const |        0         |

4. 将原来的联合索引里面列的顺序调整为code->phone->name

   | sql语句                                                      |       key        |             | 时间（单位：ms） |
   | ------------------------------------------------------------ | :--------------: | ----------- | :--------------: |
   | select * from people where  code='2027189214'                |     联合索引     | const       |        0         |
   | select * from people where  code='2027189214' and phone ='13177778888' |     联合索引     | const,const |        0         |
   | select * from people where   phone ='13177778888' and  code='2027189214' |     联合索引     | const,const |        0         |
   | select * from people where  code='2027189214' and name ='People_299999' | name列的普通索引 | const       |        0         |
   | select * from people where phone ='13177778888'              |     联合索引     |             |       197        |

5. 

##### 五、总结

1. 在查询条件没有加索引的情况下，如果查询单条数据，在sql语句最后加上`limit 1`效率会有提升，取决于被查询的数据在表中的位置

2. 查询条件单一且加了索引的情况下，sql语句最后加上`limit 1`与未加上时查询所花时间接近，查询第一条数据与查询最后一条数据所花时间也接近

3. 多表查询时，如果某个查询条件有索引，会提供查询效率；如果两张表关联的外键也加上索引，查询效率会更高，执行语句如下:

   ```java
   explain select * from addr a,people p where p.id = a.p_id and name ='People_10002'
   ```

   执行结果：

   |  id  | select_type | table | type | possible_keys | key  | key_len |     ref      | rows |    Extra    |
   | :--: | :---------: | :---: | :--: | :-----------: | :--: | :-----: | :----------: | :--: | :---------: |
   |  1   |   SIMPLE    |   p   | ref  | PRIMARY,name  | name |   259   |    const     |  1   | Using where |
   |  1   |   SIMPLE    |   a   | ref  |     p_id      | p_id |    4    | my_test.p.id |  1   |             |

4. 同一列有多个索引时，mysql会自动选择最优的索引

5. 联合索引会遵循最左前缀原则，这点可能与数据库版本有关系

6.  在某些情况下索引失效，比如：like, not in, is not, between and, <, >, !=以及使用函数

7. 关于or查询，如果条件是都有索引的，则会使用索引；如果某个条件没有索引，则会导致索引失效