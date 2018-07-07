#!/usr/bin/env bash

echo "获取构建脚本成功"

tempFileName="template.zip"

echo "请输入工程名字:"
read project

if [ -d "${project}" ]; then
    echo "${project}已经存在，是否删除(y/n)?"
    read del
    if [ "${del}" == "y" ]; then
        rm -rf "${project}"
    else
        exit 1
    fi
fi

if [ ! -f "${tempFileName}" ]; then
    echo "下载资源文件，请稍后"
    result=`curl -s "https://raw.githubusercontent.com/zjiecode/javaweb-api-generate/master/setup/template.zip" --output  "${tempFileName}"`
fi

if [ ! -f "${tempFileName}" ]; then
    echo "下载资源文件失败，请重试"
    exit 1
fi

#解压模版压缩包
unzip "${tempFileName}" -d "${project}"
if [ $? -ne 0 ];then
    echo "解压错误"
    exit 1
fi

echo "解压模版完成"

cd "${project}"

#调用生成的脚本

PROPERTY_FILE="build.gradle" #配置文件

echo "请输入数据库地址和端口(127.0.0.1)："
read dbHost
if [ -z "${dbHost}" ]; then
    dbHost="127.0.0.1"
fi
echo "请输入数据库地址和端口(3306)："
read dbPort
if [ -z "${dbPort}" ]; then
    dbPort="3306"
fi
echo "请输入数据库名字："
read dbName
if [ -z "${dbName}" ]; then
    echo "数据库名字不能为空"
    exit 1;
fi
echo "请输入数据库登陆用户名："
read dbUser
if [ -z "${dbUser}" ]; then
    echo "数据库用户名不能为空"
    exit 1;
fi

echo "请输入数据库登陆用户密码："
read dbPassword
if [ -z "${dbPassword}" ]; then
    echo "数据库密码不能为空"
    exit 1;
fi
echo "请输入生成代码包名："
read basePackage
if [ -z "${basePackage}" ]; then
    echo "基础包名不能为空"
    exit 1;
fi

# 设置值，key，value
function setValue(){
    PROPERTY_LINE=`grep  $1 ${PROPERTY_FILE}`
    if [ -z "${PROPERTY_LINE}" ]; then
        echo "配置文件 ${PROPERTY_FILE} 中不存在属性 $1"
        exit 1
    else
        NEW_PROPERTY_LINE="\t$1 '$2'"
        sed -i -e "s/${PROPERTY_LINE}/${NEW_PROPERTY_LINE}/" ${PROPERTY_FILE}
    fi
}
echo "正在配置文件..."

setValue "dbHost" "${dbHost}"
setValue "dbPort" "${dbPort}"
setValue "dbName" "${dbName}"
setValue "dbPassword" "${dbPassword}"
setValue "dbUser" "${dbUser}"
setValue "basePackage" "${basePackage}"

echo "配置文件完成"

# 替换gradle配置
echo "开始生成代码"
./gradlew generateCode
echo "生成完毕"


if [ $? -ne 0 ];then
    echo "生成错误"
    cd ..
    rm -rf "${project}"
    exit 1
fi

path=`pwd`
cd ..
echo "清理资源"
rm "${tempFileName}"
rm setup.sh

echo "\033[32m SUCCESS \033[0m"
echo "工程地址：${path}"
echo "你启动工程或者使用IDEA打开它"
echo "linux/mac启动命令: ./gradlew bootRun"
echo "window启动命令: gradle.bat bootRun"
echo "访问地址：http://127.0.0.1:8080/"
