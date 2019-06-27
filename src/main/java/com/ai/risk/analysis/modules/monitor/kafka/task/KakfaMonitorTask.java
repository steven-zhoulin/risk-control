package com.ai.risk.analysis.modules.monitor.kafka.task;

import com.ai.risk.analysis.modules.monitor.kafka.service.IKafkaMonitorSV;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p>
 *  定时器
 * </p>
 *
 * @author lijun17
 * @since 2019-06-22
 */
@Component
public class KakfaMonitorTask {

    @Autowired
    private IKafkaMonitorSV kafkaMonitorIMpl;

    @Scheduled(cron = "*/10 * * * * ?") // 每 10s执行一次
    public void scheduled() throws InterruptedException, IOException, KeeperException {
        kafkaMonitorIMpl.sinkToInflux();
    }
}
