#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-worker.sh
## Description: 启动Worker
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
# 函数名: start_worker
# 描述: 启动worker
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_worker()
{
    nohup java -server -Xms2g -Xmx13g -classpath $CONF_DIR:$LIB_JARS com.hzgc.compare.worker.Worker > ${LOG_FILE} 2>&1 &
    echo "worker is started"
}

start_worker
