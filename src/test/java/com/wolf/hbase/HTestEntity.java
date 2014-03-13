package com.wolf.hbase;

import com.wolf.hbase.dao.Entity;
import com.wolf.hbase.dao.annotation.HColumnConfig;
import com.wolf.hbase.dao.annotation.HDaoConfig;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aladdin
 */
@HDaoConfig(columnFamily = "INFO")
public final class HTestEntity extends Entity {

    @HColumnConfig(key = true, desc = "主键")
    private String rowKey;
    @HColumnConfig(desc = "imei")
    private String imei;
    @HColumnConfig(columnFamily = "INFO", desc = "产品id")
    private String product;
    @HColumnConfig(desc = "渠道id")
    private String channelId;
    @HColumnConfig(desc = "平台")
    private String platForm;

    public String getRowKey() {
        return rowKey;
    }

    public String getImei() {
        return imei;
    }

    public String getProduct() {
        return product;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getPlatForm() {
        return platForm;
    }

    @Override
    public String getKeyValue() {
        return this.rowKey;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> entityMap = new HashMap<String, String>(8, 1);
        entityMap.put("rowKey", this.rowKey);
        entityMap.put("imei", this.imei);
        entityMap.put("product", this.product);
        entityMap.put("channelId", this.channelId);
        entityMap.put("platForm", this.platForm);
        return entityMap;
    }

    @Override
    protected void parseMap(Map<String, String> entityMap) {
        this.rowKey = entityMap.get("rowKey");
        this.imei = entityMap.get("imei");
        this.product = entityMap.get("product");
        this.channelId = entityMap.get("channelId");
        this.platForm = entityMap.get("platForm");
    }
}
