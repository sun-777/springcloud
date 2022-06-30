**SpringCloud Nacos + Hibernate JPA + OpenFeign实现RESTful的数据库记录的并发批处理**


## 一、模块接口说明

### 1、service模块（RESTful接口）

- post: http://localhost:8089/db/init
- post: http://localhost:8089/db/clear
- post: http://localhost:8089/query/teacher
- post: http://localhost:8089/query/course
- post: http://localhost:8089/query/schedule
- post: http://localhost:8089/query/multitable/schedule?id=<teacherId>
- post: http://localhost:8089/query/multitable/teacher
- post: http://localhost:8089/query/multitable/maxcountcourse

### 2、client模块（使用了OpenFeign访问service接口）

- **数据库表初始化记录**

  get: http://localhost:8210/request/db/init

- **清空数据库所有的表记录**

  get: http://localhost:8210/request/db/clear

- **单表查询：查询老师相关记录**

  get: http://localhost:8210/request/query/teacher[?id=<id>&name=<name>&gender=<gender>&birth=<birth>]

- 单表查询：查询课目相关记录

  get: http://localhost:8210/request/query/course[?id=<id>&name=<name>]

- 单表查询：查询课程安排相关记录

  get: http://localhost:8210/request/query/schedule[?id=<id>&lesson=<lesson>&weekday=<weekday>]

- 多表查询：查询指定id老师的一周所有课程安排

  get: http://localhost:8210/request/query/multitable/schedule?id=<teacherId>

- 多表查询：查询课程安排中的所有老师信息

  get: http://localhost:8210/request/query/multitable/teacher

- 多表查询：查询课程安排中数量最多的课目（有多个并列最多，也一起查询出来）

  get: http://localhost:8210/request/query/multitable/maxcountcourse





## 二、细节实现说明

- Server模块使用了SpringBoot + Nacos + Hibernate JPA + HikariCP构建
- Client模块使用了SpringBoot + Nacos + OpenFeign构建

#### 1、通过Hibernate JPA注解，将实体类与数据库表名、表字段作映射

​        定义了可序列化Function接口类SerializableFunction，通过方法引用获取属性名，避免硬编码。

#### 2、实体类中有设计多对一、一对多、一对一的映射关系

#### 3、service模块中数据库表的初始化和清除功能，是通过将批处理任务切片，将切片后的子任务加入线程池，通过调用JDBC原生批处理API，实现并发批处理

#### 4、持久化层（Mapper接口）封装；业务逻辑代码分层。





## 三、Nacos集群搭建（服务器以centos或redhat为例）

#### **1、下载Nacos-Server**

```shell
[root@centos ~]# wget -c -O /home/nacos-server-2.1.0.tar.gz https://github.com/alibaba/nacos/releases/download/2.1.0/nacos-server-2.1.0.tar.gz
```

#### **2、解压到指定路径/usr/local（路径可自定义）**

```shell
[root@centos ~]# tar -xvf /home/nacos-server-2.1.0.tar.gz -C /usr/local/
```

#### **3、修改配置文件（可通过SSH复制已配置的naocs-server副本至其他服务器节点）**

```shell
[root@centos ~]# cd /usr/local/nacos/conf
// 修改配置文件application.properties，当前集群需要配置的属性
[root@centos ~]# vi application.properties
//        db.url.0=jdbc:mysql://centos:3306/nacos?charactorEncoding=utf8
[root@centos ~]# cp cluster.conf.example cluster.conf
// 服务器防火墙需要先开放Nacos端口号8848、8849、9848、9849
// 分别在对应的服务器节点中的/etc/hosts文件中，配置hostname与服务器节点ip映射信息
// 根据服务器节点信息，将集群中的服务器节点hostname和端口号配置写入cluster.conf中
[root@centos ~]# echo "win10:8848" > cluster.conf
[root@centos ~]# echo "ubuntu:8848" >> cluster.conf
[root@centos ~]# echo "centos:8848" >> cluster.conf
```

#### 4、将/usr/local/nacos/conf/nacos-mysql.sql 脚本文件导入到mysql数据库中

```shell
// 登录到mysql，创建名为nacos的数据库
// 数据库名必须和步骤2中配置文件application.properties节点db.url.0保持一致）
// 导入nacos-mysql脚本，创建Nacos Server需要的数据库表
mysql>source /usr/local/nacos/conf/nacos-mysql.sql
```

#### 5、启动集群中所有的服务器节点

```shell
[root@centos ~]# cd /usr/local/naocs/bin
[root@centos bin]# ./startup.sh
// windows下，以管理员身份进入命令行窗口，再进入Nacos目录，输入bin\startup.cmd，回车即可
```

#### 6、查看Nacos服务是否启动

```shell
[root@centos ~]# tail -n 100 /usr/local/nacos/logs/start.out
```
