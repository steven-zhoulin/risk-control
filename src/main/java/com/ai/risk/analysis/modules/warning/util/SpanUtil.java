package com.ai.risk.analysis.modules.warning.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Steven
 */
public class SpanUtil {


	/**
	 * 获取主机IP
	 *
	 * @param strHostName
	 * @return
	 */
	public static final String getHostIp(String strHostName) {
		String[] slice = StringUtils.split(strHostName, ',');
		if (null != slice) {
			for (String hostname : slice) {
				if (!"127.0.0.1".equals(hostname)) {
					return hostname;
				}
			}
		}
		return null;
	}

}
