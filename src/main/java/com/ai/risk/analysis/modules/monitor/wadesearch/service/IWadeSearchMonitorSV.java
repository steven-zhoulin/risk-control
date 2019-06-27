package com.ai.risk.analysis.modules.monitor.wadesearch.service;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @author Steven
 */
public interface IWadeSearchMonitorSV {
    void sinkToInflux() throws InterruptedException, IOException, KeeperException;
}
