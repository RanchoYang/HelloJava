#### 一、下载

workstation-player地址：https://www.vmware.com/products/workstation-player/workstation-player-evaluation.html

ubuntu地址：

#### 二、安装

先安装workstation-player，再创建新虚拟机，安装程序光盘映像文件(iso)，注意创建虚拟机时可以自定义硬件（内存，磁盘等）

#### 三、常见命令

​	ctrl + alt + t 进入终端(或者搜索terminal)，接下来就可以用命令操作了

1. **用户操作：**

   创建root权限： sudo passwd root

   切换到root用户： su root（用户名）

   查看ip：ip addr

   添加用户：useradd user_name(用户名)

   更改用户密码：passwd user_name(用户名)

2. **文件夹操作**

   创建文件夹：mkdir file_name(文件夹名)

   递归创建文件夹：mkdir  file_name/file_name/...

   删除空文件夹：rmdir file_name 

   递归删除文件夹： rmdir file_name/file_name...

   复制文件：cp  file_name(文件名) destination_file(目的文件夹)

   > 注意：常用参数如下
   >
   > ​	-a：将文件的特性一起复制
   >
   > ​	-p：连同文件的属性一起复制，常用于备份
   >
   > ​	-i：若目标文件已经存在，覆盖时先询问
   >
   > ​	-r：递归持续复制，用于目录的复制行为
   >
   > ​	-u：目标文件与源文件有差异时才会复制

   重命名：mv origin_file_name(原文件名) new_file_name(新文件名)

   移动文件：mv file_name1 file_name2 .. destination_file(目的文件夹)

   > ​	**注意：**常用参数如下
   >
   > ​	-f：如果目标文件已经存在，直接覆盖
   >
   > ​	-i：如果目标文件已经存在，先询问是否覆盖
   >
   > ​	-u：如果目标文件已经存在，且比目标文件新才会跟新

   ***在创建，复制，移动，删除等命令的时候，可以与下列命令一起使用，下列命令也可以组合使用，如rmdir -pv file_name***

   > ​	-v 不是必须命令，用来显示执行结果，常与-p一同使用
   >
   > ​	-p 递归创建/删除目录

   进入文件夹：cd file_name(文件夹名字)

   回到上级目录：cd ../

   回到根目录：cd ../..

   显示当前目录：pwd

   显示当前文件夹下所有文件：ls

   > ​	**注意：**以下命令可以组合使用
   >
   > ​	-l：列出长数据串，包含属性和权限等
   >
   > ​	-a：列出全部文件，连同隐藏文件
   >
   > ​	-d：列出目录本身
   >
   > ​	-h：将文件容量以
   >
   > ​	**-R**：连同子目录的内容一起列出(**注意为大写的R**)

   

3. **其他**

4. **linux其他操作**

   查看版本：cat /proc/version

#### 四、常见问题

1. command 'rpm' not found, but can be installed with: apt install rpm

   解决方法：运行命令：sudo apt-get install rpm

2. command 'yum' not found, but can be installed with: apt install yum

   解决方法：运行命令：sudo apt-get install yum

3. unable to locate package

   1.sudo apt-get update   这步是更新软件列表

   2.sudo apt-get upgrade 这步是更新软件

4. 是

5. 是





