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
sh generateCode.sh
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

echo "\033[32m SUCCESS \033[0m"
echo "工程地址：${path}"
echo "你启动工程或者使用IDEA打开它"
echo "linux/mac启动命令: ./gradlew bootRun"
echo "window启动命令: gradle.bat bootRun"
echo "访问地址：http://127.0.0.1:8080/"
