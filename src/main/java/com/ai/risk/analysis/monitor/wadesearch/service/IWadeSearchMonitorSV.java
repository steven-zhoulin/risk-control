package com.ai.risk.analysis.monitor.wadesearch.service;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @author Steven
 */
public interface IWadeSearchMonitorSV {
    void sinkToInflux() throws InterruptedException, IOException, KeeperException;
}
