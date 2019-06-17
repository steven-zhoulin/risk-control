package com.ai.risk.analysis.modules.warning.service.impl;

import com.ai.risk.analysis.modules.warning.entity.po.Warning;
import com.ai.risk.analysis.modules.warning.entity.unit.CallUnit;
import com.ai.risk.analysis.modules.warning.mapper.WarningMapper;
import com.ai.risk.analysis.modules.warning.service.IWarningSV;
import com.ai.risk.analysis.modules.warning.util.HbaseOps;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Steven
 * @since 2019-06-04
 */
@Slf4j
@Service
public class WarningServiceImpl extends ServiceImpl<WarningMapper, Warning> implements IWarningSV {

	/**
	 * hbase生命周期
	 */
	@Value("${spring.hbase.ttl}")
	private int ttl;

	/**
	 * 阈值
	 */
	@Value("${risk.threshold}")
	private double threshold;

	@Autowired
	private HbaseOps hbaseOps;

	private static final int DAYS_30 = 30;

	/**
	 * 预警分析
	 *
	 * @param tableName
	 * @param timestamp
	 * @param list
	 * @throws ParseException
	 */
	@Override
	public void warning(String tableName, String timestamp, List<CallUnit> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		log.info("预警数据量：" + list.size() + "条");
		Date date = DateUtils.parseDate(timestamp, "yyyyMMddHH");

		for (int i = 0 ; i < list.size() ; i++) {
			CallUnit callUnit = list.get(i);
			int days = 0;
			long cnt = 0;
			int allDays = 1;

			String name = callUnit.getName();
			while (days < DAYS_30) {
				Date previousDate = DateUtils.addDays(date, -1);
				String rowKey = DateFormatUtils.format(previousDate, "yyyyMMddHH") + "-" + name;
				date = previousDate;

				Result result = selectByRowKey(tableName, rowKey);
				if (!result.isEmpty()) {
					if (isWaringData(rowKey)) {
						// 只有没有预警过的数据才计入平均值统计
						continue;
					}
					cnt += Long.valueOf(Bytes.toString(result.getValue(HbaseOps.CF_BASE, HbaseOps.COL_CNT)));
					days++;
				}

				allDays++;
				if (allDays >= (ttl / 24 / 60 / 60)) {
					break;
				}
			}

			long currCount = callUnit.getCount();
			if(days > 0){
				long average = cnt / days;
				if (((currCount - average) / average) >= threshold) {
					// 如果超过设定阈值则插入预警表
					insertWaring(callUnit, timestamp);

					String msg = String.format("%s 调用预警, %s 被调次数: %d, 前30天均值: %d", tableName, callUnit.getName(), currCount, average);
					log.info(msg);
				}
			}

		}
	}

	private void insertWaring(CallUnit callUnit, String timestamp) throws Exception {
		Date date = DateUtils.parseDate(timestamp, "yyyyMMddHH");
		String name = DateFormatUtils.format(date, "yyyyMMddHH") + "-" + callUnit.getName();
		Warning warning = new Warning();
		warning.setName(name);
		warning.setCnt(callUnit.getCount());
		warning.setIsWarning("N");
		warning.setCreateDate(LocalDateTime.now());
		save(warning);
	}

	/**
	 * 是否预警过
	 *
	 * @param name
	 * @return
	 */
	private boolean isWaringData(String name) {
		QueryWrapper<Warning> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(Warning::getName, name);
		int ret = count(queryWrapper);
		if (ret > 0) {
			return true;
		}
		return false;
	}

	private Result selectByRowKey(String tableName, String rowKey) throws IOException {
		HTable hTable = hbaseOps.getHbaseTable(tableName);
		Get get = new Get(Bytes.toBytes(rowKey));
		Result result = hTable.get(get);
		return result;
	}

}
