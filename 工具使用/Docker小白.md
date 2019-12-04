# Docker小白

[TOC]

##### 一、前言

最近在linux服务器上面搭建docker环境部署springboot项目，坎坎坷坷终于搭了一套简单的环境，这里做个记录，里面的内容相对简单，也有很多总结不够好的地方，仅供参考

##### 二、Docker 环境搭建

##### 三、常见命令

> $imageId：镜像id
>
> $name：名字
>
> $containerId：容器id
>
> $networkId：网络id

1. docker images 查询本地所有镜像
2. docker build -t test/testimage . 创建一个名字为test/testimage，tag为latest的镜像
3. docker build -t test/testimage:2019 . 创建一个名字为test/testimage，tag为2019的镜像
4. docker rmi $imageId 删除本地镜像
5. docker search $name 从仓库查询镜像，如 docker search redis
6. docker pull $name 下载指定的镜像，不加tag的话，是默认下载latest版本的镜像
7. docker ps 查询正在运行的容器
8. docker ps -a 查询所有的容器
9. docker stop $containerId 关闭某个容器
10. docker run $containerId 运行某个容器
11. docker restart $containerId 重新运行某个容器
12. docker logs $containerId 查看某个容器的日志
13. docker exec -it $containerId bash  进入命令控制台
14. docker inspect $containerId  查看指定容器的详细信息
15. docker inspect $containerId |grep IPA 查看指定容器的ip
16. docker network ls 查询docker网络地址
17. docker network rm $name 删除指定的网络
18. docker network create -d bridge my_net 创建名为my_net的网络
19. docker network create --subnet=172.22.0.0/16 my_net 创建名为my_net的固定ip段的网络
20. docker network inspect $networkId 查看指定的network

##### 四、搭建zookeeper

1. 搜索最新的zookeeper镜像

   ```
   docker search zookeeper
   ```

2. 下载镜像到本地

   ```
   docker pull zookeeper
   ```

3. 创建并运行zookeeper容器

   ```
   docker run -d --restart=always --name zookeeper -p 9181:2181 zookeeper
   ```

   - [ ] -d 在后台运行容器
   - [ ] --restart=always docker 重启后，自动重启zookeeper容器
   - [ ] --name zookeeper 指定该容器名为zookeeper
   - [ ] -p 3181:2181 把容器的端口映射到宿主机的端口3181

##### 五、搭建mysql

1. 搜索最新的mysql镜像

   ```
   docker search mysql
   ```

2. 下载镜像到本地

   ```
   docker pull mysql
   ```

3. 创建并运行mysql容器

   ```
   docker run -it --name mysql -p 9306:3306 -v /data/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci 
   ```

   - [ ]  -v /data/mysql:/var/lib/mysql 将容器目录var/lib/mysql里面的数据挂载到宿主机data/mysql目录下
   - [ ] -e MYSQL_ROOT_PASSWORD=root 设置root用户登录密码为root
   - [ ] --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci 设置编码格式

4. 进入mysql容器内部，用于操作数据库

   ```
   docker exec -it mysql容器id bash
   ```

   1. 登录mysql，执行命令：**mysql -uroot -p** 
   2. 输入密码
   3. 执行sql

##### 六、搭建redis

1. 简单搭建，创建指定ip，指定映射端口，指定密码的redis容器，可根据需求适当修改命令：

   ```
   docker run -d --name my_redis --net redis_net --ip 172.22.0.9 -p 6684:6379 redis --requirepass "mypassword"
   ```

2. 搭建redis集群(一主两从)

   1. 分别创建3个redis容器的挂载目录，redis/redis_master，redis/redis_slave1，redis/redis_slave2

   2. 分别在挂载目录里面创建redis.conf文件，文件内容如下：

      - [ ] master配置

        ```
        logfile "redis.log"
              port 6379
              dir ../data
              appendonly yes
              appendfilename appendonly.aof
              requirepass password
        ```

      - [ ] 两个salve配置

        ```
        logfile "redis.log"
              port 6379
              dir ../data
              appendonly yes
              appendfilename appendonly.aof
              requirepass password
              slaveof 172.22.0.2 6379
              masterauth password
        ```

   3. 分别启动三个容器

      - [ ] docker run -p 6379:6379 --net redis_net -v /redis/redis_master:/data --name redis_6379 -d redis redis-server redis.conf
      - [ ] docker run -p 6380:6379 --net redis_net -v /redis/redis_slave1:/data --name redis_6380 -d redis redis-server redis.conf
      - [ ] docker run -p 6381:6379 --net redis_net -v /redis/redis_slave2:/data --name redis_6381 -d redis redis-server redis.conf

   4. 第三步已经部署好了redis集群了，还可以继续部署哨兵

   5. 分别进入主从节点所在的挂载目录，创建sentinel.conf，内容如下：

      ```
      logfile "sentinel.log"
            sentinel monitor mymaster 172.22.0.2 6379 1
            sentinel auth-pass mymaster password
      ```

   6. 分别启动sentinel容器

      1. [ ] docker run -p 26379:26379 -v /redis/redis_master/:/data --name redis_26379 -d redis redis-sentinel sentinel.conf
      2. [ ] docker run -p 26380:26379 -v /redis/redis_slave1/:/data --name redis_26380 -d redis redis-sentinel sentinel.conf
      3. [ ] docker run -p 26381:26379 -v /redis/redis_slave2/:/data --name redis_26381 -d redis redis-sentinel sentinel.conf

   7. 检测是否部署完成

      1. 进入容器内部

      2. 登录客户端，执行命令：

         ```
         redis-cli -h 127.0.0.1 -p 26379
         ```

      3. 执行命令：

         ```
         info
         ```

   8. 缺陷：通过这种方式搭建的哨兵模式，在主redis拓机后重新启动，好像不能把新的主redis的数据同步过来，有时间再补上。。。

##### 七、搭建nginx

1. 下载nginx镜像

2. 运行命令：

   ```
   docker run -d -p 80:80 --name nginx -v /nginx/static:/usr/share/nginx/static -v /nginx/conf/nginx.conf:/etc/nginx/nginx.conf -v /nginx/logs:/var/log/nginx -v /nginx/conf.d:/etc/nginx/conf.d nginx
   ```

   - [ ] /nginx/static 用来放置静态网页等
   - [ ] /nginx/conf/nginx.conf nginx的配置文件
   - [ ] /nginx/logs 日志
   - [ ] /nginx/conf.d 详细的配置文件

3. 复制conf文件到挂载目录，通过执行命令：

   ```
   docker cp 容器id:/etc/nginx/nginx.conf /nginx/conf
   ```

4. 此时查看nginx.conf,发现里面默认配置读取所有conf.d目录下的conf文件：

   ```
    include /etc/nginx/conf.d/*.conf;
   ```

5. 进入conf.d文件夹，创建一个新的conf文件，并编辑内容

   ```
   server {
       listen       80;
       server_name 域名;
       location / {
          proxy_pass http://IP:端口;
         }
       error_page   500 502 503 504  /50x.html;
       location = /50x.html {
       root   /usr/share/nginx/static;
       }
   }
   ```

   - [ ] server_name 域名：监听指定的域名
   - [ ] listen       80：监听80的端口
   - [ ] proxy_pass http://IP:端口 重定向到具体的地址
   - [ ] root   /usr/share/nginx/static：指定50x.html页面的地址

6. http协议的域名，重启容器即可生效，https协议的域名需要继续配置证书

7. 创建一个新的证书目录，nginx/cert, 把域名对应的证书和key放入其中

8. 修改conf.d文件夹下的conf文件，增加https端口443的监听

   ```
   server {
       listen       443 ssl;
       server_name 域名;
   
       ssl_certificate /cert/证书文件.pem; #我这里会在运行docker命令的时候，把cert挂载到放置证书的目录，所以这里使用相对目录就行
       ssl_certificate_key /cert/证书的key.key;
   
       ssl_session_timeout 5m;
       ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
       ssl_prefer_server_ciphers on;
       ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
   
       location / {
          proxy_pass http://IP:端口;
         }
       error_page   500 502 503 504  /50x.html;
       location = /50x.html {
       root   /usr/share/nginx/static;
       }
   }
   ```

9. 执行命令：

   ```
   docker run -d -p 80:80 -p 443:443 --net java --name nginx -v /nginx/static:/usr/share/nginx/static -v /nginx/conf/nginx.conf:/etc/nginx/nginx.conf -v /nginx/logs:/var/log/nginx -v /nginx/conf.d:/etc/nginx/conf.d -v /nginx/cert:/cert nginx
   ```

10. 通过nginx访问静态资源

    1. 首先把静态资源放置在/nginx/static目录下面

    2. 修改conf.d文件夹下的conf文件，增加静态文件的配置

       ```
        location /static/{
                       root /usr/share/nginx;
               }
       ```

    3. 重启nginx容器，如果在static目录下增加一个test.txt的文件，则可以通过如下方式访问到：

       - [ ] http://域名/static/test.txt

11. 

##### 八、发布springboot项目

1. 创建Dockerfile，文件内容如下

   ```
   FROM openjdk
   MAINTAINER rancho
   ADD 你的jar.jar app.jar
   EXPOSE 8080
   RUN bash -c 'touch /app.jar'
   ENTRYPOINT ["java","-jar","app.jar","--spring.config.location=/config/application.yml"]
   ```

2. 创建镜像：将dockerfile文件和你要发布的jar放在同一目录下，执行命令

   ```
   docker build -t /你的镜像名:2019 .
   ```

3. 创建完成，可以使用命令查看创建的镜像

   ```
   docker images
   ```

4. 创建并启动容器

   ```
   docker run -dit --name 容器名 -p 8080:8080 --net java_net -v /java/config:/config 你的镜像名:2019
   ```

##### 九、踩过的坑

1. 通过nginx访问发布的服务时，如果nginx和服务分别指定了不同的网络，当配置的proxy_pass的地址为服务容器的ip地址，nginx无法访问到，需要把proxy_pass的地址更改为主机ip；如果该服务器没有对外开放该端口，则nginx找不到发布的服务
2. 访问静态资源，配置Location路径的时候，如果配置为/static，nginx在寻找资源时会将static拼接起来,如配置的地址为root /usr/share/nginx，实际寻找的地址为/usr/share/nginx/static/
3. 部署springboot项目，dockerfile文件很重要，需要再了解更多

