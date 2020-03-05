## windows环境简单搭建ELK查看日志

1. 下载安装es
   1. 下载地址：https://www.elastic.co/cn/downloads/elasticsearch
   
      **注意：**es各个版本对java的依赖版本不一样，es 5需要java 8，es 6.5需要java 11
   
   2. 打开`bin\elasticsearch.bat`启动es
   
   3. http://localhost:9200，查看es状态
   
2. 简单的es操作命令
   1. 健康状态查询：`curl -X GET localhost:9200/_cat/health?v`
   2. 节点查询：`curl -X GET localhost:9200/_cat/nodes?v`
   3. 索引查询：`curl -X GET localhost:9200/_cat/indices?v`
   4. 创建一个索引：`curl -X PUT localhost:9200/索引名`
   5. 删除一个索引：`curl -X DELETE localhost:9200/索引名`
   
3. 配置logstash

   1. 下载地址：https://www.elastic.co/cn/downloads/logstash

   2. 在`logstash/bin`目录下创建一个新的`server.conf`文件，文件名自己定义

   3. `server.conf`内容如下

      ```
      input {
          file {
              path => ["E:/logs/server.log"]	
      		start_position => beginning
           }
      }
      
      output { 
        elasticsearch { 
          hosts => ["localhost:9200"]
      	index => "server"
         }
      }
      ```

      **path：**日志存放地址

      **start_position：**监听文件的位置，默认是end

      **index：**定义的es的index，可以定义成动态的index名字；如果不定义则会默认为logstash-xxxx的格式

   4. 命令台进入logstash的bin目录，输入 `logstash -f server.conf`

      注意：`server.conf`需替换为第二步常见的conf文件名

4. 安装kibana

   1. 下载地址：https://www.elastic.co/cn/downloads/kibana
   2. 在`kibana.yml`中配置es地址
   3. 打开`bin\kibana.bat`启动kibana
   4. http://localhost:5601，进入可视化界面
   5. 点击`Dev Tools`，再点击`Console`，输入`GET /server/`查看通过stashlog创建的index，也可以输入其他es命令
   6. 点击`Management`，再点击`Index Patterns`来设置想查询的index，我这里设置为`server*`，默认为`logstast-*`
   7. 点击`Discover`，左上角选择index pattern后，即可开始查询

5. 其他问题持续发现中。。。

