package com.ai.risk.analysis.modules.monitor.wadesearch.entity;

/**
 * @author Steven
 */

import lombok.Data;

import java.util.List;

@Data
public class SearchStat {

	/**
	 * 实例启动时间
	 */
	private String bootTime;

	/**
	 * 搜素信息
	 */
	private List<Single> searchinfo;

}
