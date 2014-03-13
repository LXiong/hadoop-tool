package com.wolf.hbase.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author aladdin
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface HColumnConfig {

    /**
     * 是否主键
     *
     * @return
     */
    public boolean key() default false;

    /**
     * 默认列族
     *
     * @return
     */
    String columnFamily() default "";

    /**
     * 查询时，若htable 列不存在时则返回该默认值
     * @return 
     */
    String defaultValue() default "";

    /**
     * 描述
     *
     * @return
     */
    public String desc();
}
