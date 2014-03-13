package com.wolf.hbase.htable;

import java.util.List;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

/**
 *
 * @author aladdin
 */
public interface HTableHandler {

    public void put(String tableName, Put put);

    public void put(String tableName, List<Put> putList);

    public void delete(String tableName, String keyValue);

    public void delete(String tableName, List<String> keyValues);

    public Result get(String tableName, String keyValue);

    public Result[] get(String tableName, List<String> keyValues);

    public boolean isTableExists(String tableName);
}
