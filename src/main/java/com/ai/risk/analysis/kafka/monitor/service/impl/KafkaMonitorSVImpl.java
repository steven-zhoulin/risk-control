package com.ai.risk.analysis.kafka.monitor.service.impl;

import com.ai.risk.analysis.kafka.monitor.LagMonitor;
import com.ai.risk.analysis.kafka.monitor.service.IKafkaMonitorSV;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.MapUtils;
import org.apache.zookeeper.KeeperException;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaMonitorSVImpl implements IKafkaMonitorSV {

    @Autowired
    private InfluxDB influxDB;

    @Override
    public void sinkToInflux() throws InterruptedException, IOException, KeeperException {
        LagMonitor traceRdbMonitor = new LagMonitor("LOG4X-TRACE-TOPIC", "rdbms");
        Map<Integer, ImmutableMap> traceRdb = traceRdbMonitor.getNewOffsetData();

        LagMonitor traceHbaseMonitor = new LagMonitor("LOG4X-TRACE-TOPIC", "hbase");
        Map<Integer, ImmutableMap> traceHbase = traceHbaseMonitor.getNewOffsetData();

        if (MapUtils.isNotEmpty(traceRdb)) {
            long currentTimeMillis = System.currentTimeMillis();
            Set<Integer> keySet = traceHbase.keySet();
            for (Integer key : keySet) {
                ImmutableMap immutableMap = traceHbase.get(key);
                long l4xRdbmsOffset = 0;
                long produceRdbmsOffset = 0;
                if (MapUtils.isNotEmpty(traceRdb.get(key))) {
                    l4xRdbmsOffset = Long.valueOf(traceRdb.get(key).get("consumerOffset").toString());
                    produceRdbmsOffset = Long.valueOf(traceRdb.get(key).get("produceOffset").toString());
                }

                writeToTrace(String.valueOf(key), Long.valueOf(immutableMap.get("produceOffset").toString()), produceRdbmsOffset, Long.valueOf(immutableMap.get("consumerOffset").toString()), l4xRdbmsOffset, currentTimeMillis);
            }
        }


//        LagMonitor logMonitor = new LagMonitor("LOG4X-LOG-TOPIC", "");
//        Map<Integer, ImmutableMap> log = logMonitor.getNewOffsetData();
//        if(MapUtils.isNotEmpty(log)){
//            long currentTimeMillis = System.currentTimeMillis();
//            Set<Integer> keySet = log.keySet();
//            for (Integer key : keySet) {
//                ImmutableMap immutableMap = log.get(key);
//                writeToLog(String.valueOf(key), Long.valueOf(immutableMap.get("produceOffset").toString()), Long.valueOf(immutableMap.get("consumerOffset").toString()), currentTimeMillis);
//            }
//        }


        LagMonitor metricMonitor = new LagMonitor("LOG4X-METRIC-TOPIC", "");
        Map<Integer, ImmutableMap> metric = metricMonitor.getNewOffsetData();
        if (MapUtils.isNotEmpty(metric)) {
            long currentTimeMillis = System.currentTimeMillis();
            Set<Integer> keySet = metric.keySet();
            for (Integer key : keySet) {
                ImmutableMap immutableMap = metric.get(key);
                writeToMetric(String.valueOf(key), Long.valueOf(immutableMap.get("produceOffset").toString()), Long.valueOf(immutableMap.get("consumerOffset").toString()), currentTimeMillis);
            }
        }
    }

    /**
     * @param partitionId    分区ID
     * @param produceOffset  生产偏移量
     * @param l4xHbaseOffset hbase消费偏移量
     * @param l4xRdbmsOffset rbd消费偏移量
     */
    private void writeToTrace(String partitionId, long produceHbaseOffset, long produceRdbmsOffset, long l4xHbaseOffset, long l4xRdbmsOffset, long currentTimeMillis) {
        Point point = Point.measurement("kafka_trace_topic")
                .tag("partition_id", partitionId)
                .addField("produce_hbase_offset", produceHbaseOffset)
                .addField("produce_rdbms_offset", produceRdbmsOffset)
                .addField("l4x_hbase_offset", l4xHbaseOffset)
                .addField("l4x_rdbms_offset", l4xRdbmsOffset)
                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                .build();

        influxDB.write("kafka", "autogen", point);
    }

    /**
     * @param partitionId   分区ID
     * @param produceOffset 生产偏移量
     * @param l4xLogOffset  log消费偏移量
     */
    private void writeToLog(String partitionId, long produceOffset, long l4xLogOffset, long currentTimeMillis) {
        Point point = Point.measurement("kafka_log_topic")
                .tag("partition_id", partitionId)
                .addField("produce_offset", produceOffset)
                .addField("l4x_log_offset", l4xLogOffset)
                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                .build();

        influxDB.write("kafka", "autogen", point);
    }

    /**
     * @param partitionId   分区ID
     * @param produceOffset 生产偏移量
     * @param l4xLogOffset  metric消费偏移量
     */
    private void writeToMetric(String partitionId, long produceOffset, long l4xMetricOffset, long currentTimeMillis) {
        Point point = Point.measurement("kafka_metric_topic")
                .tag("partition_id", partitionId)
                .addField("produce_offset", produceOffset)
                .addField("l4x_metric_offset", l4xMetricOffset)
                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                .build();

        influxDB.write("kafka", "autogen", point);
    }
}
