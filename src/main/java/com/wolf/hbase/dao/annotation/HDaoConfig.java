package com.wolf.hbase.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author aladdin
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface HDaoConfig {

    /**
     * htable表名
     *
     * @return
     */
    String tableName() default "";

    /**
     * 默认列族
     *
     * @return
     */
    String columnFamily() default "";
}
