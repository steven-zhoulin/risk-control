package com.ai.risk.analysis.modules.warning.util;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供hbase相关函数
 *
 * @author lijun17
 */
@Component
public class HbaseOps {

    public static final byte[] CF_BASE = Bytes.toBytes("base");
    public static final byte[] COL_CNT = Bytes.toBytes("cnt");

    private static Connection connection;

    public final Connection getConnection(String address) {
        if (null == connection) {
            connection = HbaseConnectionFactory.getHbaseConnection(address);
        }
        return connection;
    }

    /**
     * hbase连接地址
     */
    @Value("${spring.hbase.zookeeper.server-list}")
    private String address;

    /**
     * hbase生命周期
     */
    @Value("${spring.hbase.ttl}")
    private int ttl;

    /**
     * 获取hbase表
     */
    public HTable getHbaseTable(String tableName) throws IOException {

        HTable table = HTABLE_MAP.get(tableName);
        if (null != table) {
            return table;
        }

        Connection con = getConnection(address);
        TableName tName = TableName.valueOf(tableName);
        Admin admin = con.getAdmin();
        if (!admin.tableExists(tName)) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(tName);
            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(CF_BASE);
            hColumnDescriptor.setTimeToLive(ttl);
            tableDescriptor.addFamily(hColumnDescriptor);
            admin.createTable(tableDescriptor);
        }
        table = (HTable) con.getTable(tName);
        table.setAutoFlushTo(false);
        HTABLE_MAP.put(tableName, table);

        return table;
    }

    private static final Map<String, HTable> HTABLE_MAP = new HashMap<>();

}
