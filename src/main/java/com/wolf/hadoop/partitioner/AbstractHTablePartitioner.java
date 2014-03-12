package com.wolf.hadoop.partitioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 *
 * @author aladdin
 */
public abstract class AbstractHTablePartitioner extends Partitioner<Text, Text> implements Configurable {

    public static final String TABLE_REGION_PART = "-Dmapred.tableRegionPart";
    /*
     * region startKe集合，00000000～fffffff顺序存储 
     */
    private final List<String> regionList = new ArrayList<String>(512);
    /*
     * region 和 partition对应关系
     */
    private final Map<String, Integer> regionPartMap = new HashMap<String, Integer>(512, 1);
    private Configuration conf;

    public abstract String getRowKey(Text key, Text value);

    @Override
    public final int getPartition(Text key, Text value, int numPartitions) {
        int result;
        String rowKey = this.getRowKey(key, value);
        String region = "00000000";
        String regionNext;
        for (int index = 0; index < this.regionList.size(); index++) {
            region = this.regionList.get(index);
            if ((index + 1) >= this.regionList.size()) {
                //最后一个region
                break;
            } else {
                //非最后一个region
                regionNext = this.regionList.get(index + 1);
                if (rowKey.compareTo(region) >= 0 && rowKey.compareTo(regionNext) < 0) {
                    //定位当前part所属的region
                    break;
                }
            }
        }
        result = this.regionPartMap.get(region);
        return result;
    }

    @Override
    public final void setConf(Configuration conf) {
        this.conf = conf;
        //获取conf中hTable的region分布信息
        String regionPartString = this.conf.get(AbstractHTablePartitioner.TABLE_REGION_PART);
        String[] regionParts = regionPartString.split("\t");
        String[] region;
        String startKey;
        int part;
        for (String regionPart : regionParts) {
            region = regionPart.split("_", 2);
            startKey = region[1];
            part = Integer.parseInt(region[0]);
            this.regionList.add(startKey);
            this.regionPartMap.put(startKey, part);
            System.out.println("------------startKey:" + startKey + " part:" + part);
        }
    }

    @Override
    public final Configuration getConf() {
        return this.conf;
    }

    public static void initJobByHTablePartition(final Job job, final String tableName, final int loadFactor) throws IOException {
        HTable hTable = new HTable(job.getConfiguration(), tableName);
        NavigableMap<HRegionInfo, ServerName> regionMap = hTable.getRegionLocations();
        Set<Entry<HRegionInfo, ServerName>> entrySet = regionMap.entrySet();
        String startKey;
        String endKey;
        String hostName;
        HRegionInfo hRegionInfo;
        ServerName serverName;
        //获取region server的数量
        LinkedList<String> reginLinkedList;
        final List<String> regionHostList = new ArrayList<String>(32);
        final Map<String, LinkedList> regionHostMap = new HashMap<String, LinkedList>(32, 1);
        System.out.println("--region server host");
        for (Entry<HRegionInfo, ServerName> entry : entrySet) {
            serverName = entry.getValue();
            hostName = serverName.getHostname();
            if (regionHostMap.containsKey(hostName) == false) {
                reginLinkedList = new LinkedList<String>();
                System.out.println("----region server:" + hostName);
                regionHostList.add(hostName);
                regionHostMap.put(hostName, reginLinkedList);
            }
        }
        System.out.println("--region server host--");
        System.out.println("--region partition");
        //记录每个region的startKey，并根据region所在的region server分类
        final List<String> startKeyList = new ArrayList<String>(512);
        for (Entry<HRegionInfo, ServerName> entry : entrySet) {
            serverName = entry.getValue();
            hostName = serverName.getHostname();
            reginLinkedList = regionHostMap.get(hostName);
            hRegionInfo = entry.getKey();
            startKey = Bytes.toString(hRegionInfo.getStartKey());
            startKey = startKey.toLowerCase();
            if (startKey.isEmpty()) {
                startKey = "00000000";
            }
            reginLinkedList.offer(startKey);
            startKeyList.add(startKey);
            endKey = Bytes.toString(hRegionInfo.getEndKey());
            endKey = endKey.toLowerCase();
            System.out.println("---hostName:" + hostName + " startKey:" + startKey + " endKey:" + endKey);
        }
        System.out.println("--region partition--");
        System.out.println("--region part");
        //为每个region分配区号
        StringBuilder partInfoBuilder = new StringBuilder(10240);
        final Map<String, Integer> partMap = new HashMap<String, Integer>(startKeyList.size(), 1);
        int partIndex = 0;
        int part;
        boolean unFinished = true;
        while (unFinished) {
            unFinished = false;
            for (String regionHost : regionHostList) {
                reginLinkedList = regionHostMap.get(regionHost);
                if (reginLinkedList.isEmpty() == false) {
                    part = partIndex;
                    //取值
                    for (int index = 0; index < loadFactor; index++) {
                        startKey = reginLinkedList.poll();
                        if (startKey != null) {
                            partMap.put(startKey, part);
                        }
                    }
                    partIndex++;
                    unFinished = true;
                }
            }
        }
        //保存region分区信息
        for (String sKey : startKeyList) {
            part = partMap.get(sKey);
            partInfoBuilder.append(Integer.toString(part)).append('_').append(sKey).append("\t");
            System.out.println("-----startKey:" + sKey + " part:" + part);
        }
        partInfoBuilder.setLength(partInfoBuilder.length() - "\t".length());
        System.out.println("----------------region part----------------");
        //获取numReduceTask的数量
        int numReduceTask = partIndex;
        System.out.println("------------NumReduceTasks:" + numReduceTask);
        job.setNumReduceTasks(numReduceTask);
        job.getConfiguration().set(AbstractHTablePartitioner.TABLE_REGION_PART, partInfoBuilder.toString());
    }
}
