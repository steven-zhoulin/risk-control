package com.ai.risk.analysis.modules.monitor.wadesearch.entity;

import lombok.Data;

/**
 * @author Steven
 */
@Data
public class Single {

	/**
	 * 搜索编码
	 */
	private String searchCode;

	/**
	 * 搜索次数
	 */
	private long cnt;

	/**
	 * 搜索总耗时 (ms)
	 */
	private long ttc;

	/**
	 * 搜索平均耗时 (ms)
	 */
	private double avg;

}
