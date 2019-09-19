#### 一、安装并配置必要的依赖关系

1. 更新软件列表：sudo apt-get update

2. 安装依赖：sudo apt-get install -y curl openssh-server ca-certificates

3. 安装postfix发送通知邮件：sudo apt-get install -y postfix

   如果出现一个配置屏幕，选择 internet站点

#### 二、安装gitlab

1. 添加gitlab软件包到存储库：

   ```
   curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ee/script.deb.sh | sudo bash
   ```

2. 安装gitlab软件包：

   ```
   sudo EXTERNAL_URL="http://gitlab.example.com" apt-get install gitlab-ee
   ```

   注意：
   
   ​	1.将上面url http://gitlab.example.com 替换成你的url
   
   ​	2.如果你的url使用了https,则需要配置ssl

#### 三、其他