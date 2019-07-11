**一、前言**

​	dubbo-admin分为server和ui两部分，在git上可以查看源码，并且有使用指南

**二、下载运行dubbo-admin**

	1.下载地址：https://github.com/apache/dubbo-admin
	2.下载完成后，可以更改zookeeper注册地址，如果zk是在本地则不需要更改：
		配置文件地址：
			dubbo-admin-server/src/main/resources/application.properties
		配置文件属性：
			admin.registry.address=zookeeper://127.0.0.1:2181
			admin.config-center=zookeeper://127.0.0.1:2181
			admin.metadata-report.address=zookeeper://127.0.0.1:2181
	3.通过以下命令打包dubbo-admin-server项目：mvn clean package
		打包成功后进入dubbo-admin-server\target目录可以找到jar包：dubbo-admin-server-0.1.jar
	4.使用cmd进入admin-server\target目录,运行命令：java -jar dubbo-admin-0.1.jar
	5.启动成功后，可以访问http://localhost:8080查看
		注意:默认端口是8080，如果希望使用其他端口，则第4点使用如下命令（8082替换成你想要的端口）：java -jar dubbo-admin-0.1.jar --server.port=8082

**三、功能介绍**