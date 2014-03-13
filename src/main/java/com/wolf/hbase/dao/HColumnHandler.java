package com.wolf.hbase.dao;

/**
 *
 * @author aladdin
 */
public interface HColumnHandler {

    public String getColumnName();
    
    public String getColumnFamily();
    
    public String getDefaultValue();
    
    public String getDesc();
}
