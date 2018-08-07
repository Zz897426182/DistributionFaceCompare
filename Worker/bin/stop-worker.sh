#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    stop-worker.sh
## Description: 停止Worker
## Author:      wujiaqi
## Created:     2018-01-08
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                               ### bin目录
cd ..
COMPARE_DIR=`pwd`                                           ### compare目录
LOG_DIR=${COMPARE_DIR}/log                                  ### log目录
LOG_FILE=${LOG_DIR}/worker.log                              ### log文件
CONF_DIR=${COMPARE_DIR}/conf                                ### conf目录
LIB_DIR=${COMPARE_DIR}/lib                                  ### lib目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

if [ ! -d ${LOG_DIR} ]; then
    mkdir ${LOG_DIR}
fi


#####################################################################
# 函数名:stop_worker
# 描述: 停止worker
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function stop_worker()
{
    WORKER_PID=`jps | grep Worker | awk '{print $1}'`
    if [ -n "${WORKER_PID}" ];then
        echo "worker is exist, exit with 0, kill service now"
        kill -9 ${WORKER_PID}
        echo "stop worker successfull"
    else
        echo "worker is not start"
    fi
}

#####################################################################
# 函数名: main
# 描述: 脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    stop_worker
}

main
