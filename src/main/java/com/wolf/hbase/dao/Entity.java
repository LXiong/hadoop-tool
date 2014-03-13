package com.wolf.hbase.dao;

import java.util.Map;

/**
 * 所有实体抽象父类
 *
 * @author aladdin
 */
public abstract class Entity {

    /**
     * 获取该实体的key值
     *
     * @return
     */
    public abstract String getKeyValue();

    /**
     * 实体信息转换成Map
     *
     * @return
     */
    public abstract Map<String, String> toMap();

    /**
     * 解析map信息，为实体的field赋值
     *
     * @param entityMap
     */
    protected abstract void parseMap(final Map<String, String> entityMap);

    /**
     * 重载实体的toString方法，输出实体的所有field信息
     *
     * @return
     */
    @Override
    public final String toString() {
        return this.toMap().toString();
    }
}
