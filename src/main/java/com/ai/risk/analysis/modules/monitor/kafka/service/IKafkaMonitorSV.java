package com.ai.risk.analysis.modules.monitor.kafka.service;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * <p>
 *  kafka数据聚合
 * </p>
 *
 * @author lijun17
 * @since 2019-06-22
 */
public interface IKafkaMonitorSV {
    void sinkToInflux() throws InterruptedException, IOException, KeeperException;
}
