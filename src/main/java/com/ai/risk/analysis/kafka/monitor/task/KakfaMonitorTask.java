package com.ai.risk.analysis.kafka.monitor.task;

import com.ai.risk.analysis.kafka.monitor.service.IKafkaMonitorSV;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KakfaMonitorTask {

    @Autowired
    private IKafkaMonitorSV kafkaMonitorIMpl;

    @Scheduled(cron = "*/10 * * * * ?") // 每 10s执行一次
    public void scheduled() throws InterruptedException, IOException, KeeperException {
        kafkaMonitorIMpl.sinkToInflux();
    }
}
