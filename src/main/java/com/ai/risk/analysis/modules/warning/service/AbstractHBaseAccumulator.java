package com.ai.risk.analysis.modules.warning.service;

import com.ai.risk.analysis.modules.warning.entity.unit.CallUnit;
import com.ai.risk.analysis.modules.warning.util.HbaseOps;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Steven
 */
public class AbstractHBaseAccumulator {

	@Autowired
	private IWarningSV warningSVImpl;

	@Autowired
	private HbaseOps hbaseOps;

	public void sinkToHBase(String tableName, String time, Map<String, AtomicLong> oldLocalCounts) {

		List<CallUnit> list = new ArrayList(5000);
		for (String name : oldLocalCounts.keySet()) {
			long cnt = oldLocalCounts.get(name).get();
			CallUnit unit = new CallUnit(name, cnt);
			list.add(unit);
		}
		oldLocalCounts.clear();

		try {
			HTable hTable = hbaseOps.getHbaseTable(tableName);
			List<Put> puts = new ArrayList<>();
			for (CallUnit callUnit : list) {
				String rowKey = time + "-" + callUnit.getName();
				Put put = new Put(Bytes.toBytes(rowKey));
				put.addColumn(HbaseOps.CF_BASE, HbaseOps.COL_CNT, Bytes.toBytes(String.valueOf(callUnit.getCount())));
				puts.add(put);
			}

			if (puts.size() > 0) {
				hTable.put(puts);
				hTable.flushCommits();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 预警分析
		try {
			warningSVImpl.warning(tableName, time, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
