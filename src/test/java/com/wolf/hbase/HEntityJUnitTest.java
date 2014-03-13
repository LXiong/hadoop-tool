package com.wolf.hbase;

import com.wolf.hadoop.utils.Md5Util;
import com.wolf.hbase.dao.HEntityDao;
import com.wolf.hbase.dao.HEntityDaoBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author aladdin
 */
public class HEntityJUnitTest {

    private HEntityDao<HTestEntity> testEntityDao;

    public HEntityJUnitTest() {
        HEntityDaoBuilder builder = new HEntityDaoBuilder(HTestEntity.class);
        this.testEntityDao = builder.setTableName("Test_for_liujy").build();
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    //

    @Test
    public void testInsert() {
        String channelId = "1";
        String platForm = "1";
        String product = "1";
        String imei = "1";
        String part = Md5Util.encryptByMd5(imei);
        part = part.toLowerCase().substring(0, 4);
        String rowKey = part + "_" + product + "_" + imei;
        Map<String, String> insertMap = new HashMap<String, String>(8, 1);
        insertMap.put("rowKey", rowKey);
        insertMap.put("channelId", channelId);
        insertMap.put("platForm", platForm);
        insertMap.put("product", product);
        insertMap.put("imei", imei);
        this.testEntityDao.insert(insertMap);
    }

    @Test
    public void testUpdate() {
        String channelId = "2";
        String product = "1";
        String imei = "1";
        String part = Md5Util.encryptByMd5(imei);
        part = part.toLowerCase().substring(0, 4);
        String rowKey = part + "_" + product + "_" + imei;
        Map<String, String> updateMap = new HashMap<String, String>(8, 1);
        updateMap.put("rowKey", rowKey);
        updateMap.put("channelId", channelId);
        this.testEntityDao.update(updateMap);
    }

    @Test
    public void testInquire() {
        String product = "1";
        String imei = "1";
        String part = Md5Util.encryptByMd5(imei);
        part = part.toLowerCase().substring(0, 4);
        String rowKey = part + "_" + product + "_" + imei;
        HTestEntity testEntity = this.testEntityDao.inquireByKey(rowKey);
        System.out.println(testEntity);
    }

    @Test
    public void testDelete() {
        String product = "1";
        String imei = "1";
        String part = Md5Util.encryptByMd5(imei);
        part = part.toLowerCase().substring(0, 4);
        String rowKey = part + "_" + product + "_" + imei;
        this.testEntityDao.delete(rowKey);
    }

    @Test
    public void testBatchInsert() {
        String channelId = "1";
        String platForm = "1";
        String product = "1";
        String imei;
        String part;
        String rowKey;
        Map<String, String> insertMap;
        List<Map<String, String>> insertMapList = new ArrayList<Map<String, String>>(100);
        long start = 10000;
        for (int index = 0; index < 100; index++) {
            imei = Long.toString(start);
            part = Md5Util.encryptByMd5(imei);
            part = part.toLowerCase().substring(0, 4);
            rowKey = part + "_" + product + "_" + imei;
            insertMap = new HashMap<String, String>(8, 1);
            insertMap.put("rowKey", rowKey);
            insertMap.put("channelId", channelId);
            insertMap.put("platForm", platForm);
            insertMap.put("product", product);
            insertMap.put("imei", imei);
            insertMapList.add(insertMap);
            start++;
        }
        this.testEntityDao.batchInsert(insertMapList);
    }

    @Test
    public void testBatchUpdate() {
        String channelId = "2";
        String product = "1";
        String imei;
        String part;
        String rowKey;
        Map<String, String> updateMap;
        List<Map<String, String>> insertMapList = new ArrayList<Map<String, String>>(100);
        long start = 10000;
        for (int index = 0; index < 100; index++) {
            imei = Long.toString(start);
            part = Md5Util.encryptByMd5(imei);
            part = part.toLowerCase().substring(0, 4);
            rowKey = part + "_" + product + "_" + imei;
            updateMap = new HashMap<String, String>(8, 1);
            updateMap.put("rowKey", rowKey);
            updateMap.put("channelId", channelId);
            insertMapList.add(updateMap);
            start++;
        }
        this.testEntityDao.batchUpdate(insertMapList);
    }

    @Test
    public void testInquireList() {
        String product = "1";
        String imei;
        String part;
        String rowKey;
        List<String> keyList = new ArrayList<String>(100);
        long start = 10000;
        for (int index = 0; index < 100; index++) {
            imei = Long.toString(start);
            part = Md5Util.encryptByMd5(imei);
            part = part.toLowerCase().substring(0, 4);
            rowKey = part + "_" + product + "_" + imei;
            keyList.add(rowKey);
            start++;
        }
        List<HTestEntity> testEntityList = this.testEntityDao.inquireByKeys(keyList);
        for (HTestEntity hTestEntity : testEntityList) {
            System.out.println(hTestEntity);
        }
    }

    @Test
    public void testBatchDelete() {
        String product = "1";
        String imei;
        String part;
        String rowKey;
        List<String> keyList = new ArrayList<String>(100);
        long start = 10000;
        for (int index = 0; index < 100; index++) {
            imei = Long.toString(start);
            part = Md5Util.encryptByMd5(imei);
            part = part.toLowerCase().substring(0, 4);
            rowKey = part + "_" + product + "_" + imei;
            keyList.add(rowKey);
            start++;
        }
        this.testEntityDao.batchDelete(keyList);
    }
}