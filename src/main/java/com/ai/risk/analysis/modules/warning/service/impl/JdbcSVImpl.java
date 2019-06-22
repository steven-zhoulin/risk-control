package com.ai.risk.analysis.modules.warning.service.impl;

import com.ai.risk.analysis.modules.warning.entity.unit.PointUnit;
import com.ai.risk.analysis.modules.warning.service.IJdbcSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 */
@Slf4j
@Service
public class JdbcSVImpl implements IJdbcSV {

	private Map<String, PointUnit> accumulator = new ConcurrentHashMap<>();

	@Autowired
	private InfluxDB influxDB;

	@Value("${risk.seperatorChar}")
	private char seperatorChar;

	@Override
	public void localAccumulation(String tableName, String action, int elapsedTime) {
		String key = tableName + seperatorChar + action;
		PointUnit pointUnit = accumulator.get(key);
		if (null == pointUnit) {
			pointUnit = new PointUnit();
			accumulator.put(key, pointUnit);
		}

		// 递增调用次数
		pointUnit.getCnt().incrementAndGet();

		// 递增调用耗时
		pointUnit.getTtc().addAndGet(elapsedTime);

		// 条件满足时,设置最高耗时
		if (elapsedTime < pointUnit.getMinCost()) {
			pointUnit.setMinCost(elapsedTime);
		}

		// 条件满足时,设置最高耗时
		if (elapsedTime > pointUnit.getMaxCost()) {
			pointUnit.setMaxCost(elapsedTime);
		}
	}

	@Override
	public void sinkToInflux() {
		long start = System.currentTimeMillis();
		Set<String> keySet = accumulator.keySet();
		if (null == keySet) {
			return;
		}

		int count = 0;
		for (String key : keySet) {
			PointUnit pointUnit = accumulator.get(key);

			long cnt = pointUnit.getCnt().longValue();
			long ttc = pointUnit.getTtc().longValue();
			int minCost = pointUnit.getMinCost();
			int maxCost = pointUnit.getMaxCost();

			// 计数器清零
			pointUnit.getCnt().set(0L);
			pointUnit.getTtc().set(0L);
			pointUnit.setMinCost(Integer.MAX_VALUE);
			pointUnit.setMaxCost(0);

			if (0 == cnt || cnt < 15) {
				// 只统计周期内总调用次数大于阀值的
				continue;
			}

			// 平均耗时，毫秒
			long cost = ttc / cnt;

			String[] slice = StringUtils.split(key, seperatorChar);
			String tableName = slice[0];
			String action = slice[1];

			write("jdbc", tableName, "action", action, cnt, cost, minCost, maxCost);
			count++;
		}

		long cost = System.currentTimeMillis() - start;
		log.info(String.format("聚合: %6d 条, 耗时(ms): %5d", keySet.size(), cost));
	}

	/**
	 *
	 * @param tableName 表名
	 * @param tagName
	 * @param cnt 调用次数
	 * @param cost 总耗时
	 */
	private void write(String measurement, String tableName, String tagName, String tagValue, long cnt, long cost, int minCost, int maxCost) {
		Point point = Point.measurement(measurement)
			.tag("tableName", tableName)
			.tag(tagName, tagValue)
			.addField("cnt", cnt)
			.addField("cost", cost)
			.addField("minCost", minCost)
			.addField("maxCost", maxCost)
			.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
			.build();

		influxDB.write(point);
	}
}
