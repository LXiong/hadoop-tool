package com.wolf.hbase.dao;

import com.wolf.hbase.htable.HTableHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author aladdin
 */
public final class HEntityDaoImpl<T extends Entity> implements HEntityDao<T> {

    private final Class<T> clazz;
    private final String tableName;
    private final HTableHandler hTableHandler;
    private final HColumnHandler keyHandler;
    private final List<HColumnHandler> columnHandlerList;

    public HEntityDaoImpl(String tableName, HTableHandler hTableHandler, Class<T> clazz, HColumnHandler keyHandler, List<HColumnHandler> columnHandlerList) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.hTableHandler = hTableHandler;
        this.keyHandler = keyHandler;
        this.columnHandlerList = columnHandlerList;
    }

    /**
     * 解析map数据，实例化clazz
     *
     * @param entityMap
     * @return
     */
    private T newInstance(Map<String, String> entityMap) {
        T t;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            System.err.println("instancing  class " + this.clazz.getName() + "error.Cause:" + e.getMessage());
            throw new RuntimeException("instancing class error".concat(clazz.getName()));
        }
        t.parseMap(entityMap);
        return t;
    }

    private Map<String, String> readResultToMap(Result result) {
        Map<String, String> resultMap = new HashMap<String, String>(this.columnHandlerList.size() + 1, 1);
        //放入key
        String keyValue = Bytes.toString(result.getRow());
        resultMap.put(this.keyHandler.getColumnName(), keyValue);
        //读取column
        String columnName;
        String columnFamily;
        byte[] columnValue;
        for (HColumnHandler columnHandler : columnHandlerList) {
            columnName = columnHandler.getColumnName();
            columnFamily = columnHandler.getColumnFamily();
            columnValue = result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
            if (columnValue == null) {
                resultMap.put(columnName, columnHandler.getDefaultValue());
            } else {
                resultMap.put(columnName, Bytes.toString(columnValue));
            }
        }
        return resultMap;
    }

    private T readResult(Result result) {
        Map<String, String> resultMap = this.readResultToMap(result);
        return this.newInstance(resultMap);
    }

    private List<T> readResult(Result[] result) {
        List<T> tList = new ArrayList<T>(result.length);
        T t;
        for (int index = 0; index < result.length; index++) {
            t = this.readResult(result[index]);
            tList.add(t);
        }
        return tList;
    }

    private Put createInsertPut(final String keyValue, final Map<String, String> entityMap) {
        final byte[] rowKey = Bytes.toBytes(keyValue);
        final Put put = new Put(rowKey);
        String columnName;
        String columnFamily;
        String columnValue;
        for (HColumnHandler columnHandler : this.columnHandlerList) {
            columnName = columnHandler.getColumnName();
            columnFamily = columnHandler.getColumnFamily();
            columnValue = entityMap.get(columnName);
            if (columnValue == null) {
                System.err.println("insert H table " + this.tableName + " failure message: can not find column:" + columnName);
                System.err.println("insert failure value" + entityMap.toString());
                throw new RuntimeException("insert failure message: can not find column value...see log");
            } else {
                put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(columnValue));
            }
        }
        return put;
    }

    private Put createUpdatePut(final String keyValue, final Map<String, String> dataMap) {
        final byte[] rowKey = Bytes.toBytes(keyValue);
        final Put put = new Put(rowKey);
        String columnName;
        String columnFamily;
        String columnValue;
        for (HColumnHandler columnHandler : this.columnHandlerList) {
            columnName = columnHandler.getColumnName();
            columnFamily = columnHandler.getColumnFamily();
            columnValue = dataMap.get(columnName);
            if (columnValue != null) {
                put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(columnValue));
            }
        }
        return put;
    }

    @Override
    public T inquireByKey(String keyValue) {
        T t = null;
        Result result = this.hTableHandler.get(this.tableName, keyValue);
        if (result != null) {
            t = this.readResult(result);
        }
        return t;
    }

    @Override
    public List<T> inquireByKeys(List<String> keyValues) {
        List<T> tList;
        Result[] result = this.hTableHandler.get(this.tableName, keyValues);
        if (result != null && result.length > 0) {
            tList = this.readResult(result);
        } else {
            tList = new ArrayList<T>(0);
        }
        return tList;
    }

    @Override
    public String insert(Map<String, String> entityMap) {
        final String keyName = this.keyHandler.getColumnName();
        String keyValue = entityMap.get(keyName);
        if (keyValue == null) {
            System.err.println("insert H table " + this.tableName + " failure message: can not find key:" + keyName);
            System.err.println("insert failure value" + entityMap.toString());
            throw new RuntimeException("insert failure message: can not find key value...see log");
        }
        final Put put = this.createInsertPut(keyValue, entityMap);
        this.hTableHandler.put(this.tableName, put);
        return keyValue;
    }

    @Override
    public T insertAndInquire(Map<String, String> entityMap) {
        final String keyName = this.keyHandler.getColumnName();
        String keyValue = entityMap.get(keyName);
        if (keyValue == null) {
            System.err.println("insert H table " + this.tableName + " failure message: can not find key:" + keyName);
            System.err.println("insert failure value" + entityMap.toString());
            throw new RuntimeException("insert failure message: can not find key value...see log");
        }
        final Put put = this.createInsertPut(keyValue, entityMap);
        this.hTableHandler.put(this.tableName, put);
        T t = this.newInstance(entityMap);
        return t;
    }

    @Override
    public void batchInsert(List<Map<String, String>> entityMapList) {
        final String keyName = this.keyHandler.getColumnName();
        List<Put> putList = new ArrayList<Put>(entityMapList.size());
        Put put;
        String keyValue;
        for (Map<String, String> entityMap : entityMapList) {
            keyValue = entityMap.get(keyName);
            if (keyValue == null) {
                System.err.println("insert H table " + this.tableName + " failure message: can not find key:" + keyName);
                System.err.println("insert failure value" + entityMap.toString());
                throw new RuntimeException("insert failure message: can not find key value...see log");
            }
            put = this.createInsertPut(keyValue, entityMap);
            putList.add(put);
        }
        this.hTableHandler.put(this.tableName, putList);
    }

    @Override
    public String update(Map<String, String> entityMap) {
        final String keyName = this.keyHandler.getColumnName();
        String keyValue = entityMap.get(keyName);
        if (keyValue == null) {
            System.err.println("update H table " + this.tableName + " failure message: can not find key:" + keyName);
            System.err.println("update failure value" + entityMap.toString());
            throw new RuntimeException("update failure message: can not find key value...see log");
        } else {
            final Put put = this.createUpdatePut(keyValue, entityMap);
            if (put.isEmpty() == false) {
                this.hTableHandler.put(this.tableName, put);
            }
        }
        return keyValue;
    }

    @Override
    public void batchUpdate(List<Map<String, String>> entityMapList) {
        final String keyName = this.keyHandler.getColumnName();
        List<Put> putList = new ArrayList<Put>(entityMapList.size());
        Put put;
        String keyValue;
        for (Map<String, String> entityMap : entityMapList) {
            keyValue = entityMap.get(keyName);
            if (keyValue == null) {
                System.err.println("update H table " + this.tableName + " failure message: can not find key:" + keyName);
                System.err.println("update failure value" + entityMap.toString());
                throw new RuntimeException("update failure message: can not find key value...see log");
            }
            put = this.createUpdatePut(keyValue, entityMap);
            if (put.isEmpty() == false) {
                putList.add(put);
            }
        }
        if (putList.isEmpty() == false) {
            this.hTableHandler.put(this.tableName, putList);
        }
    }

    @Override
    public T updateAndInquire(Map<String, String> entityMap) {
        final String keyName = this.keyHandler.getColumnName();
        String keyValue = entityMap.get(keyName);
        if (keyValue == null) {
            System.err.println("update H table " + this.tableName + " failure message: can not find key:" + keyName);
            System.err.println("update failure value" + entityMap.toString());
            throw new RuntimeException("update failure message: can not find key value...see log");
        } else {
            final Put put = this.createUpdatePut(keyValue, entityMap);
            if (put.isEmpty() == false) {
                this.hTableHandler.put(this.tableName, put);
            }
        }
        return this.inquireByKey(keyValue);
    }

    @Override
    public void delete(String keyValue) {
        this.hTableHandler.delete(this.tableName, keyValue);
    }

    @Override
    public void batchDelete(List<String> keyValues) {
        this.hTableHandler.delete(this.tableName, keyValues);
    }
}
