#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-table.sh
## Description:  创建HBase表
## Author:       wujiaqi
## Created:      2017-11-28
################################################################################

#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                               ### bin目录
SQL_DIR=${BIN_DIR}/sql
cd ..
COMPARE_DIR=`pwd`
LOG_DIR=${COMPARE_DIR}/log
LOG_FILE=${LOG_DIR}/worker.log

source /opt/hzgc/env_bigdata.sh
HBASE_HOME=${HBASE_HOME}


if [ ! -d ${LOG_DIR} ]; then
    mkdir ${LOG_DIR}
fi


#####################################################################
# 函数名: create_table
# 描述: 创建hbase表
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_table(){
    echo "**********************************************" | tee -a $LOG_FILE
    echo "please waitinng, 创建表， “faceData”........"  | tee -a $LOG_FILE
    sh ${HBASE_HOME}/bin/hbase shell ${SQL_DIR}/hbase.sql
    if [ $? = 0 ];then
        echo "创建成功..."
    else
        echo "创建失败..."
    fi
}

create_table
