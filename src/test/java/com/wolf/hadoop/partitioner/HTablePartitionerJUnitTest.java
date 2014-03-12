package com.wolf.hadoop.partitioner;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.mapreduce.Job;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aladdin
 */
public class HTablePartitionerJUnitTest {
    
    public HTablePartitionerJUnitTest() {
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
     public void testPartition() throws IOException {
         Configuration conf = HBaseConfiguration.create();
         final Job job = new Job(conf, "test");
         AbstractHTablePartitioner.initJobByHTablePartition(job, "DATA_Product_IMEI", 8);
     }
}