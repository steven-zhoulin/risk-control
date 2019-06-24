package com.ai.risk.analysis.kafka.monitor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import me.gavincook.monitor.MonitorConfig;
import me.gavincook.monitor.util.JsonConverter;
import me.gavincook.monitor.util.MapUtil;
import me.gavincook.monitor.util.Pair;
import me.gavincook.monitor.util.TypeUtil;
import me.gavincook.monitor.zk.ZKFetcher;
import me.gavincook.monitor.zk.ZKManager;
import org.apache.commons.collections.MapUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LagMonitor {
    private ZKFetcher kafkaZkFetcher;
    private ZKFetcher stormZKFetcher;
    private Map<Integer, Broker> brokers = new HashMap();
    private List<PartitionMetadata> partitionMetadatas = new ArrayList();
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String topic;
    private String lag;
    private Map<Integer, Pair<Long, PartitionMetadata>> lastestOffsets = new HashMap();
    private Map<Integer, Long> consumerOffsets = new HashMap();
    private Map<Integer, Pair<Long, PartitionMetadata>> lastestOffsetsOther = new HashMap();
    private Map<Integer, Long> consumerOffsetsOther = new HashMap();
    private Map<Integer, String> hosts = new HashMap();
    private boolean useOther = false;
    private String monitorLayout = MonitorConfig.getLayMonitorLayout();
    private Long monitorPeriod = MonitorConfig.getLayMonitorPeriod();

    public LagMonitor(String topic, String lag)
            throws IOException, KeeperException, InterruptedException
    {
        this.kafkaZkFetcher = ZKManager.getKafkaZKFetcher();
        this.stormZKFetcher = ZKManager.getStormZKFetcher();
        this.topic = topic;
        this.lag = lag;
    }

    public void fetchOffset()
            throws InterruptedException
    {
        fetchKafkaOffset();
        fetchStormOffset();
    }

    public void refresh()
            throws InterruptedException
    {
        this.useOther = (!this.useOther);
        fetchStormOffset();
        fetchPartitionsOffset();
        long sumLag = 0L;
        for (Integer partitionId : getLastestOffsetsMap().keySet())
        {
            Pair partitionMetadataPair = (Pair)getLastestOffsetsMap().get(partitionId);
            Pair oldPartitionMetadataPair = (Pair)getLastestOffsetsMapHistory().get(partitionId);

            sumLag = sumLag + (((Long)partitionMetadataPair.getKey()).longValue() - ((Long)MapUtil.getOrDefault(getConsumerOffsetsMap(), partitionId, Long.valueOf(0L))).longValue());
            System.out.println(this.monitorLayout.replaceAll("%partitionId", String.valueOf(partitionId))
                    .replaceAll("%ps",
                            String.valueOf(
                                    (((Long)partitionMetadataPair.getKey()).longValue() - ((Long)oldPartitionMetadataPair.getKey()).longValue()) / this.monitorPeriod.longValue()))
                    .replaceAll("%kafkaoffset",
                            String.valueOf(partitionMetadataPair
                                    .getKey()))
                    .replaceAll("%consumeroffset",
                            String.valueOf(MapUtil.getOrDefault(getConsumerOffsetsMap(), partitionId, Long.valueOf(0L))))
                    .replaceAll("%lag",
                            String.valueOf(
                                    ((Long)partitionMetadataPair.getKey()).longValue() - ((Long)MapUtil.getOrDefault(getConsumerOffsetsMap(), partitionId, Long.valueOf(0L))).longValue()))
                    .replaceAll("%cs",
                            String.valueOf((((Long)MapUtil.getOrDefault(getConsumerOffsetsMap(), partitionId, Long.valueOf(0L))).longValue() - ((Long)MapUtil.getOrDefault(getConsumerOffsetsMapHistory(), partitionId, Long.valueOf(0L))).longValue()) / this.monitorPeriod.longValue())));
        }
        System.out.println(" Sum of Lag is " + sumLag);
    }

    public Map<Integer, ImmutableMap> getNewOffsetData()
    {
        fetchKafkaBrokers();
        fetchPartitionMetadata();
        fetchStormOffset();
        fetchPartitionsOffset();
        Map<Integer, ImmutableMap> resultData = new TreeMap();
        for (Integer partitionId : getLastestOffsetsMap().keySet())
        {
            Pair partitionMetadataPair = (Pair)getLastestOffsetsMap().get(partitionId);

            ImmutableMap map = ImmutableMap.builder().put("produceOffset", String.valueOf(partitionMetadataPair.getKey())).put("consumerOffset", String.valueOf(MapUtil.getOrDefault(getConsumerOffsetsMap(), partitionId, Long.valueOf(0L)))).put("host", MapUtil.getOrDefault(hosts, partitionId, "127.0.0.1")).build();
            resultData.put(partitionId, map);
        }
        return resultData;
    }

    private void fetchKafkaOffset()
    {
        fetchKafkaBrokers();
        fetchPartitionMetadata();
        fetchPartitionsOffset();
    }

    private void fetchPartitionsOffset()
    {
        for (PartitionMetadata partitionMetadata : this.partitionMetadatas) {
            fetchBrokerOffset(partitionMetadata);
        }
    }

    private void fetchKafkaBrokers()
    {
        List<String> brokersIds = this.kafkaZkFetcher.getChildren("/brokers/ids");
        for (String brokerIdStr : brokersIds)
        {
            Map brokerData = (Map)JsonConverter.convert(this.kafkaZkFetcher.getData("/brokers/ids/" + brokerIdStr), HashMap.class);
            int brokerId = Integer.valueOf(brokerIdStr).intValue();
            this.brokers.put(Integer.valueOf(brokerId), new Broker(brokerId, brokerData.get("host").toString(), ((Integer)TypeUtil.convert(brokerData.get("port"), Integer.class)).intValue()));
            this.logger.info("Fetch kafka brokers: {}", brokerData.get("host") + ":" + brokerData.get("port"));
        }
    }

    private void fetchPartitionMetadata()
    {
        Iterator localIterator1 = this.brokers.values().iterator();
        if (localIterator1.hasNext())
        {
            Broker broker = (Broker)localIterator1.next();
            SimpleConsumer consumer = new SimpleConsumer(broker.host(), broker.port(), 5000, 2048, "simple");
            TopicMetadataRequest request = new TopicMetadataRequest(new ArrayList() {});
            TopicMetadataResponse response = consumer.send(request);
            List<TopicMetadata> topicMetadatas = response.topicsMetadata();
            if ((topicMetadatas == null) || (topicMetadatas.size() == 0)) {
                consumer.close();
            }
            for (TopicMetadata topicMetadata : topicMetadatas) {
                if (topicMetadata.topic().equals(this.topic))
                {
                    this.partitionMetadatas = topicMetadata.partitionsMetadata();

                    break;
                }
            }
            consumer.close();
            return;
        }
    }

    private synchronized void fetchBrokerOffset(PartitionMetadata partitionMetadata)
    {
        SimpleConsumer consumer = new SimpleConsumer(partitionMetadata.leader().host(), partitionMetadata.leader().port(), 5000, 2048, "simple");
        Map requestInfo = new HashMap();
        TopicAndPartition topicAndPartition = new TopicAndPartition(this.topic, partitionMetadata.partitionId());
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.LatestTime(), 1));

        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), "simple");
        OffsetResponse response = consumer.getOffsetsBefore(request);

        long[] offsets = response.offsets(this.topic, partitionMetadata.partitionId());
        if ((offsets != null) && (offsets.length > 0)) {
            getLastestOffsetsMap().put(Integer.valueOf(partitionMetadata.partitionId()), new Pair(
                    Long.valueOf(response
                            .offsets(this.topic,
                                    Integer.valueOf(partitionMetadata
                                            .partitionId()).intValue())[0]), partitionMetadata));
        }
        consumer.close();
    }

    private synchronized void fetchStormOffset()
    {
        String basePath = MonitorConfig.getStormBasePathForTopic(this.topic + (Strings.isNullOrEmpty(this.lag) ? "" : new StringBuilder().append(".").append(this.lag).toString()));
        List<String> partitions = this.stormZKFetcher.getChildren(basePath);
        int length = "partition_".length();
        for (String partition : partitions)
        {
            Integer partitionId = Integer.valueOf(partition.substring(length));
            Long offset = (Long)JsonConverter.getKey(this.stormZKFetcher.getData(basePath + "/" + partition), "offset", Long.class);
            Map broker = (Map)JsonConverter.getKey(this.stormZKFetcher.getData(basePath + "/" + partition), "broker", Map.class);
            if(MapUtils.isNotEmpty(broker)){
                hosts.put(partitionId, broker.get("host").toString());
            }
            getConsumerOffsetsMap().put(partitionId, offset);
        }
    }

    private Map<Integer, Pair<Long, PartitionMetadata>> getLastestOffsetsMap()
    {
        return this.useOther ? this.lastestOffsetsOther : this.lastestOffsets;
    }

    private Map<Integer, Long> getConsumerOffsetsMap()
    {
        return this.useOther ? this.consumerOffsetsOther : this.consumerOffsets;
    }

    private Map<Integer, Pair<Long, PartitionMetadata>> getLastestOffsetsMapHistory()
    {
        return this.useOther ? this.lastestOffsets : this.lastestOffsetsOther;
    }

    private Map<Integer, Long> getConsumerOffsetsMapHistory()
    {
        return this.useOther ? this.consumerOffsets : this.consumerOffsetsOther;
    }

    public static void main(String[] args)
            throws InterruptedException, IOException, KeeperException
    {
        LagMonitor monitor = new LagMonitor("LOG4X-TRACE-TOPIC", "rdbms");
        System.out.println(monitor.getNewOffsetData());
    }
}
