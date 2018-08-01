## javaweb-api-generate

![author](https://img.shields.io/badge/author-zjiecode-green.svg?longCache=true&style=flat)
![star](https://img.shields.io/redmine/plugin/stars/redmine_xlsx_format_issue_exporter.svg)
![language](https://img.shields.io/badge/language-java-blue.svg)
![PRs-welcome](https://img.shields.io/badge/PRs-welcome-green.svg?longCache=true&style=flat)
[![license](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/zjiecode/javaweb-api-generate.svg?branch=master)](https://travis-ci.org/zjiecode/javaweb-api-generate)
![version](https://img.shields.io/badge/version-0.0.12-brightgreen.svg?longCache=true&style=flat)

### 简介

很多时候，想要开发一个小玩意儿，核心功能开发本来就很累了（尤其是对于个人开发者）但还要开发一个后台，针对增删改查的简单后台，写起来更累。
使用这个gradle插件，能够非常快的根据你设计的数据库，生成一个对应的api接口的网站。然后稍微修改一下，
基本就可以完成一个信息的增删改查系统了。

### 生成的代码的环境

本插件的环境：
- 推荐使用IDE：`idea`；
- 构建工具：`gradle`；
- 网站使用框架：`spring boot`；
- 使用的数据：`mysql`
### 原理

插件根据你输入的数据库，读取对应的数据库结构，生成对应的表的接口代码

### 使用方式
#### mac/linux
打开终端，复制下面代码直接执行，根据中文提示，输入对应的信息即可。
```shell
bash <(curl -s -S -L https://github.com/zjiecode/javaweb-api-generate/raw/master/setup/setup.sh)
```
#### windows
- 点击这里[https://github.com/zjiecode/javaweb-api-generate/raw/master/setup/template.zip](https://github.com/zjiecode/javaweb-api-generate/raw/master/setup/template.zip)，下载模版，
- 解压下载下来的zip包；
- 修改`builde.gradle`里面的配置信息
    ```groovy
     config {
         dbDriver 'com.mysql.jdbc.Driver'
         dbHost '127.0.0.1'
         dbPort '3306'
         dbName 'spring_boot_demo'
         dbUser 'root'
         dbPassword '123456'
         basePackage 'com.zjiecode.web.api'
     }
    ```
- 执行`gradlew.bat generateCode`
- 如果生成成功，执行`gradlew.bat bootRun` 就可以启动网站了

如果不出意外 ，就生成了网站工程，在你解压的zip包里面，直接用idea打开就可以了。

#### 给已有项目添加插件

- 添加插件依赖
    ```groovy
     classpath 'com.zjiecode:javaweb-api-generate:{last-version}'//把last-version替换成最新版本
    ```
- 添加应用插件
    ```groovy
    apply plugin: 'javaweb-api-generate'//生成代码用的插件
    ```
- 设置配置信息
    ```groovy
     config {
         dbDriver 'com.mysql.jdbc.Driver'
         dbHost '127.0.0.1'
         dbPort '3306'
         dbName 'spring_boot_demo'
         dbUser 'root'
         dbPassword '123456'
         basePackage 'com.zjiecode.web.api'
     }
    ```

到这里，就添加完了插件，刷新一下gradle，你会看到多出来一个`generateCode`任务，运行就可以了。

### 生成的网站工程介绍

#### 接口
会生成每个数据库表对应的接口，每个表会对应生成7个接口，分别如下

    【post】  /{表名}/ 插入数据；
    【put】   /{表名}/{id} 修改数据；
    【delete】/{表名}/{id} 删除数据；
    【get】   /{表名}/{id} 查询某一条数据；
    【get】   /{表名}/{pageIndex}/{pageSize} 分页查询；
    【get】   /{表名}/all 查询全部数据；
    【get】   /{表名}/count} 查询数据条数；

**生成以后，也就意味可以直接访问数据库所有数据，请务必删除不需要的接口。**

#### 接口数据结构
```js
{
    code: 402, //状态
    msg: "[(GET)/user/2]非法请求", //提示信息
    data: null //数据
}
```

#### 错误拦截

错误均已经拦截， 返回json格式数据

    SUCCESS(200),//成功
    BIZ_FAIL(400),//业务异常错误
    UNAUTHORIZED(401),//未认证
    SIGN_FAIL(402),//签名错误
    NOT_FOUND(404),//接口不存在
    INTERNAL_SERVER_ERROR(500);//服务器内部错误
    
#### 接口签名验证

为了保证接口安全 ，避免被非法调用，添加了简单的数据签名，获取所有的参数，按照字典排序，加入密钥和时间戳，做MD5运算，
服务器验证时间差距太大，或者签名不正确会拒绝请求。
签名相关的密约，可以在`application.properties`文件中配置；

签名相关实现，请查看`AppWebMvcConfigurer`类。

#### 请求方式（数据格式）
默认的post提交数据方式是：x-www-form-urlencoded，也就是采用a-1&b=2这种
如果你使用json的方式提交数据，可以在controller上注解@ResquestBody即可。

### Pull Request
任何问题，欢迎PR，PR请提交到`develop`分支,感谢.
