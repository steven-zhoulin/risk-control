package com.ai.risk.analysis.modules.monitor.wadesearch.task;

import com.ai.risk.analysis.modules.monitor.wadesearch.service.IWadeSearchMonitorSV;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Steven
 */
@Component
public class WadeSearchMonitorTask {

    @Autowired
    private IWadeSearchMonitorSV wadeSearchMonitorSVImpl;

    /**
     * 每 60s 执行一次
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws KeeperException
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scheduled() throws InterruptedException, IOException, KeeperException {
        wadeSearchMonitorSVImpl.sinkToInflux();
    }
}
