package com.hzgc.compare.worker.util;


import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HBaseHelper {
    private static Logger LOG = LoggerFactory.getLogger(HBaseHelper.class);

    private static Configuration innerHBaseConf = null;
    private static Connection innerHBaseConnection = null;
    private static Map<String, Table> tables;

    public HBaseHelper() {
        initHBaseConfiguration();
        LOG.info("Init configuration is successful");
        initHBaseConnection();
        LOG.info("Init connection is successful");
    }

    /**
     * 内部方法，用来初始化HBaseConfiguration
     */
    private static void initHBaseConfiguration() {
        try {
            innerHBaseConf = HBaseConfiguration.create();
            innerHBaseConf.addResource(ClassLoader.getSystemResourceAsStream("hbase-site.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeInnerHbaseConn() {
        if (innerHBaseConnection != null) {
            try {
                innerHBaseConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取HBaseConfiguration
     *
     * @return 返回HBaseConfiguration对象
     */
    private static Configuration getHBaseConfiguration() {
        if (null == innerHBaseConf) {
            initHBaseConfiguration();
        }
        return innerHBaseConf;
    }

    /**
     * 内部方法，用来初始化HBaseConnection
     */
    private static void initHBaseConnection() {
        try {
            innerHBaseConnection = ConnectionFactory.createConnection(HBaseHelper.getHBaseConfiguration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取HBase连接
     *
     * @return 返回HBaseConnection对象
     */
    public static Connection getHBaseConnection() {
        if (null == innerHBaseConnection || innerHBaseConnection.isClosed()) {
            LOG.info("The HBaseConnection is null or closed, create connection");
            initHBaseConnection();
        }
        return innerHBaseConnection;
    }

    /**
     * 获取表对象
     *
     * @param tableName 表名称
     * @return 表对象
     */
    public static Table getTable(String tableName) {
        if (!StringUtils.isBlank(tableName)) {
            try {
                if(tables == null){
                    tables = new HashMap<>();
                }
                Table table = tables.get(tableName);
                if(table == null){
                    System.out.println("To create The table : " + tableName);
                    table = HBaseHelper.getHBaseConnection().getTable(TableName.valueOf(tableName));
                    tables.put(tableName, table);
                }
                return table;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 关闭connection连接
     */
    public static void closeConn() {
        if (null != innerHBaseConnection && !innerHBaseConnection.isClosed()) {
            try {
                innerHBaseConnection.close();
                LOG.info("HBaseConnection close successfull");
            } catch (IOException e) {
                LOG.info("HBaseConnection close failed ");
                e.printStackTrace();
            }
        }
    }

    public static void closeTable(Table table) {
        if (null != table) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
