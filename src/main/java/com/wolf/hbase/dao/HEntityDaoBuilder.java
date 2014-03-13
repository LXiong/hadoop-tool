package com.wolf.hbase.dao;

import com.wolf.hbase.dao.annotation.HColumnConfig;
import com.wolf.hbase.dao.annotation.HDaoConfig;
import com.wolf.hbase.htable.HTableHandler;
import com.wolf.hbase.htable.HTableHandlerImpl;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * 实体数据访问对象创建类
 *
 * @author aladdin
 */
public final class HEntityDaoBuilder<T extends Entity> {

    private static HTableHandler hTableHandler;

    static {
        Configuration config = HBaseConfiguration.create();
        hTableHandler = new HTableHandlerImpl(config);
    }
    //table name
    private String tableName = "";
    //实体class
    private final Class<T> clazz;

    public HEntityDaoBuilder(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public HEntityDaoBuilder setTableName(final String tableName) {
        this.tableName = tableName;
        return this;
    }

    public HEntityDao<T> build() {
        if (this.clazz == null) {
            throw new RuntimeException("build H entityDao error. Cause: clazz is null");
        }
        //解析clazz
        if (clazz.isAnnotationPresent(HDaoConfig.class)) {
            final HDaoConfig daoConfig = clazz.getAnnotation(HDaoConfig.class);
            //获取表名
            if (this.tableName == null || this.tableName.isEmpty()) {
                //如果没有设定表名，则从注解中获取表名
                this.tableName = daoConfig.tableName();
            }
            if (this.tableName == null || this.tableName.isEmpty()) {
                throw new RuntimeException("build H entityDao error. Cause: tableName is null");
            }
            //默认列族
            final String defaultColumnFamily = daoConfig.columnFamily();
            //解析所有列
            Field[] fieldTemp = clazz.getDeclaredFields();
            HColumnHandler keyHandler = null;
            List<HColumnHandler> columnHandlerList = new ArrayList<HColumnHandler>(fieldTemp.length);
            HColumnHandler columnHandler;
            int modifier;
            String fieldName;
            HColumnConfig columnConfig;
            String columnFamily;
            for (Field field : fieldTemp) {
                modifier = field.getModifiers();
                if (Modifier.isStatic(modifier) == false) {
                    //非静态字段
                    fieldName = field.getName();
                    if (field.isAnnotationPresent(HColumnConfig.class)) {
                        //
                        columnConfig = field.getAnnotation(HColumnConfig.class);
                        if (columnConfig.key()) {
                            if (keyHandler == null) {
                                keyHandler = new HColumnHandlerImpl(fieldName, "", "", columnConfig.desc());
                            } else {
                                throw new RuntimeException("building H entityDao error:" + clazz.getName() + ". Cause:too many key");
                            }
                        } else {
                            columnFamily = columnConfig.columnFamily();
                            if (columnFamily.isEmpty()) {
                                columnFamily = defaultColumnFamily;
                            }
                            if (columnFamily.isEmpty()) {
                                throw new RuntimeException("building H entityDao error:" + clazz.getName() + ". Cause:can not find columnFamily:" + fieldName);
                            }
                            columnHandler = new HColumnHandlerImpl(fieldName, columnFamily, columnConfig.defaultValue(), columnConfig.desc());
                            columnHandlerList.add(columnHandler);
                        }
                    }
                }
            }
            if (keyHandler == null) {
                throw new RuntimeException("building H entityDao error:" + clazz.getName() + ". Cause:can not find key");
            }
            //检测表是否存在
            boolean exists = hTableHandler.isTableExists(this.tableName);
            if (exists == false) {
                throw new RuntimeException("build H entityDao error. Cause: htable:" + this.tableName + " not created");
            }
            //构造dao
            HEntityDao<T> entityDao = new HEntityDaoImpl(
                    this.tableName,
                    hTableHandler,
                    this.clazz,
                    keyHandler,
                    columnHandlerList);
            return entityDao;

        } else {
            throw new RuntimeException("build H entityDao error. Cause: missing annotation HDaoConfig");
        }
    }
}
