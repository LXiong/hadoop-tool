package com.wolf.hbase.dao;

/**
 *
 * @author aladdin
 */
public interface HColumnHandler {

    /**
     * 获取列名
     * @return 
     */
    public String getColumnName();
    
    /**
     * 获取列族
     * @return 
     */
    public String getColumnFamily();
    
    /**
     * 如果该列不存在，返回默认值。nosql动态增加新列时，使用
     * @return 
     */
    public String getDefaultValue();
    
    /**
     * 列描述
     * @return 
     */
    public String getDesc();
}
