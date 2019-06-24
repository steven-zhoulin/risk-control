package com.ai.risk.analysis.modules.warning.service;

/**
 * @author Steven
 */
public interface IJdbcSV {
	void localAccumulation(String tableName, String action, int elapsedTime);
	void sinkToInflux();
}
