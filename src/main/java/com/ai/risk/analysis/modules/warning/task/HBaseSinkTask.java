package com.ai.risk.analysis.modules.warning.task;

import com.ai.risk.analysis.modules.warning.entity.unit.CallUnit;
import com.ai.risk.analysis.modules.warning.service.IOpcodeSV;
import com.ai.risk.analysis.modules.warning.service.IServiceSV;
import com.ai.risk.analysis.modules.warning.service.IWarningSV;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HBase聚合
 *
 * @author Steven
 * @date 2019-06-04
 */
@Component
public class HBaseSinkTask {

	@Autowired
	private IServiceSV serviceSVImpl;

	@Autowired
	private IOpcodeSV opcodeSVImpl;

	//@Scheduled(cron = "0 */5 * * * ?") // 每 5 分钟执行一次
	@Scheduled(cron = "0 0 * * * ?") // 整点执行一次
	public void scheduled() {

		// 获取上一个周期的时间
		String timestamp = DateFormatUtils.format(System.currentTimeMillis() - 100000, "yyyyMMddHH");

		// 将数据下沉到 HBase
		serviceSVImpl.sinkToHBase(timestamp);
		opcodeSVImpl.sinkToHBase(timestamp);

	}

}
