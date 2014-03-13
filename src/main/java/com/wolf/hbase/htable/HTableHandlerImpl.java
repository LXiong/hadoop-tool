package com.wolf.hbase.htable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author aladdin
 */
public final class HTableHandlerImpl implements HTableHandler {

    private final Configuration config;
    private final HTablePool hTablePool;

    public HTableHandlerImpl(Configuration config) {
        this.config = config;
        this.hTablePool = new HTablePool(this.config, 1);
    }

    private HTableInterface getHTable(String tableName) {
        return this.hTablePool.getTable(tableName);
    }

    @Override
    public void put(String tableName, Put put) {
        HTableInterface hTableInterface = this.getHTable(tableName);
        try {
            hTableInterface.put(put);
        } catch (IOException ex) {
            System.err.println("HBase put table:" + tableName + " error!");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void put(String tableName, List<Put> putList) {
        HTableInterface hTableInterface = this.getHTable(tableName);
        hTableInterface.setAutoFlush(false);
        try {
            hTableInterface.put(putList);
            hTableInterface.flushCommits();
        } catch (IOException ex) {
            System.err.println("HBase list put table:" + tableName + " error!");
            throw new RuntimeException(ex);
        } finally {
            hTableInterface.setAutoFlush(true);
        }
    }

    @Override
    public void delete(String tableName, String keyValue) {
        byte[] rowKey = Bytes.toBytes(keyValue);
        Delete delete = new Delete(rowKey);
        HTableInterface hTableInterface = this.getHTable(tableName);
        try {
            hTableInterface.delete(delete);
        } catch (IOException ex) {
            System.err.println("HBase delete table:" + tableName + ",keyValue:" + keyValue + " error!");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String tableName, List<String> keyValues) {
        byte[] rowKey;
        Delete delete;
        List<Delete> deleteList = new ArrayList<Delete>(keyValues.size());
        for (String keyValue : keyValues) {
            rowKey = Bytes.toBytes(keyValue);
            delete = new Delete(rowKey);
            deleteList.add(delete);
        }
        HTableInterface hTableInterface = this.getHTable(tableName);
        hTableInterface.setAutoFlush(false);
        try {
            hTableInterface.delete(deleteList);
            hTableInterface.flushCommits();
        } catch (IOException ex) {
            System.err.println("HBase list delete table:" + tableName + " error!");
            for (String keyValue : keyValues) {
                System.err.println("----delete keyValue:" + keyValue);
            }
            throw new RuntimeException(ex);
        } finally {
            hTableInterface.setAutoFlush(true);
        }
    }

    @Override
    public Result get(String tableName, String keyValue) {
        Result result = null;
        byte[] rowKey = Bytes.toBytes(keyValue);
        Get get = new Get(rowKey);
        get.setMaxVersions();
        HTableInterface hTableInterface = this.getHTable(tableName);
        try {
            result = hTableInterface.get(get);
        } catch (IOException ex) {
            System.err.println("HBase get table:" + tableName + ",keyValue:" + keyValue + " error!");
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    public Result[] get(String tableName, List<String> keyValues) {
        Result[] result = null;
        byte[] rowKey;
        Get get;
        List<Get> getList = new ArrayList<Get>(keyValues.size());
        for (String keyValue : keyValues) {
            rowKey = Bytes.toBytes(keyValue);
            get = new Get(rowKey);
            get.setMaxVersions();
            getList.add(get);
        }
        HTableInterface hTableInterface = this.getHTable(tableName);
        try {
            result = hTableInterface.get(getList);
        } catch (IOException ex) {
            System.err.println("HBase list get table:" + tableName + ",keyValue size:" + keyValues.size() + " error!");
            for (String keyValue : keyValues) {
                System.err.println("----get keyValue:" + keyValue);
            }
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    public boolean isTableExists(String tableName) {
        boolean result;
        try {
            HBaseAdmin hbaseAdmin = new HBaseAdmin(config);
            result = hbaseAdmin.tableExists(tableName);
        } catch (IOException ex) {
            System.err.println("HBase assert table exists:" + tableName + " error!");
            throw new RuntimeException(ex);
        }
        return result;
    }
}
