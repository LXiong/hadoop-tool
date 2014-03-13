package com.wolf.hbase.dao;

import java.util.List;
import java.util.Map;

/**
 * entity dao
 *
 * @author aladdin
 */
public interface HEntityDao<T extends Entity> {

    /**
     * 根据主键查询
     *
     * @param key
     * @return
     */
    public T inquireByKey(final String keyValue);

    /**
     * 根据主键集合查询
     *
     * @param keyValues
     * @return
     */
    public List<T> inquireByKeys(final List<String> keyValues);

    /**
     * 插入,返回keyValue，实体对应的字段必须全部赋值
     *
     * @param entityMap
     */
    public String insert(final Map<String, String> entityMap);

    /**
     * 插入，并返回新增实体
     *
     * @param entityMap
     * @return
     */
    public T insertAndInquire(final Map<String, String> entityMap);

    /**
     * 批量插入，实体对应的字段必须全部赋值
     *
     * @param entityMapList
     */
    public void batchInsert(final List<Map<String, String>> entityMapList);

    /**
     * 更新,返回keyValue
     *
     * @param entityMap
     */
    public String update(final Map<String, String> entityMap);

    /**
     * 批量更新
     *
     * @param entityMapList
     */
    public void batchUpdate(final List<Map<String, String>> entityMapList);

    /**
     * 更新并查询后新后值
     *
     * @param entityMap
     * @return
     */
    public T updateAndInquire(Map<String, String> entityMap);

    /**
     * 删除
     *
     * @param keyValue
     */
    public void delete(String keyValue);

    /**
     * 批量删除
     *
     * @param keyValues
     */
    public void batchDelete(final List<String> keyValues);
}
