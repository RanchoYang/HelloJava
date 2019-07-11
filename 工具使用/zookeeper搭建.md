**一、zookeeper下载**

	1.官网：https://zookeeper.apache.org/
	2.下载地址：https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/

**二、zookeeper安装**

	1.解压下载好的zookeeper-3.4.14.tar.gz
	2.进入目录\zookeeper\conf，复制zoo_sample.cfg并且改名为zoo.cfg
	3.通过cmd进入zookeeper的bin目录，如cd D:\zookeeper\bin
	4.在bin目录下，通过zkServer.cmd命令启动zookpeer
	5.在bin目录下，通过jps命令可以查看状态，如果出现QuoromPeerMain则表示已经启动成功

**三、注意事项**

	1.单机环境下可以不配置环境变量，但需要配置jdk
	2.启动时候重复出现：no session established for client，查看如下两个属性是否设置在zookeeper同一目录下，如果是，则检查分隔开的斜杠是否正确：
	
		dataDir=D:/zookeeper/data  -->  dataDir=D:\zookeeper\data
		dataLogDir=D:/zookeeper/logs  -->  dataLogDir=D:\zookeeper\logs
	
	3.启动zookeeper后不要关闭启动小黑屏
	4.启动时候出现小黑屏闪退，可能是jdk版本与zookeeper不匹配

**四、zookeeper集群**

	未完待续。。。。