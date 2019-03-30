#!/bin/bash
export TZ=Asia/Shanghai
java -jar empty_room_get.jar
time=$(date "+%Y-%m-%d %H:%M:%S")
echo "$time 成功抓取数据" >> emptyroom_exec.log
