package com.wolf.hbase.dao;

/**
 *
 * @author aladdin
 */
public final class HColumnHandlerImpl implements HColumnHandler {

    private final String columnName;
    private final String desc;
    private final String columnFamily;
    private final String defaultValue;

    public HColumnHandlerImpl(final String columnName, final String columnFamily, final String defaultValue, final String desc) {
        this.columnName = columnName;
        this.columnFamily = columnFamily;
        this.defaultValue = defaultValue;
        this.desc = desc;
    }

    @Override
    public String getColumnName() {
        return this.columnName;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String getColumnFamily() {
        return this.columnFamily;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }
}
