package com.ai.risk.analysis.modules.warning.kafka.thread;

import com.ai.risk.analysis.modules.warning.kafka.Queue;
import com.ai.risk.analysis.modules.warning.service.*;
import com.ai.risk.analysis.modules.warning.util.SpanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Steven
 */
@Slf4j
@Component
public class ServiceConsumer extends Thread {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private IServiceAndOpcodeSV serviceAndOpcodeSVImpl;

	@Autowired
	private IServiceAndIpSV ServiceAndIpSVImpl;

	@Autowired
	private IServiceAndInstanceSV serviceAndInstanceSVImpl;

	@Autowired
	private IServiceSV serviceSVImpl;

	@Autowired
	private IOpcodeSV opcodeSVImpl;

	private AtomicLong index = new AtomicLong(0L);

	@Override
	@PostConstruct
	public void start() {
		super.start();
		log.info("ServiceConsumer started...");
	}

	@Override
	public void run() {

		while (true) {

			try {
				String value = Queue.pollService();
				if (null == value) {
					continue;
				}
				Map span = mapper.readValue(value, Map.class);
				String callType = (String) span.get("callType");
				if (!"CSF".equals(callType)) {
					continue;
				}

				String serviceName = (String) span.get("serviceName");
				Long startTime = (Long) span.get("startTime");
				Integer elapsedTime = (Integer) span.get("elapsedTime");
				String ip = SpanUtil.getHostIp((String) span.get("hostName"));
				String instance = (String) span.get("appName");
				// boolean success = (boolean) span.get("success");
				Map extMap = (Map) span.get("ext");
				String opCode = (String) extMap.get("opCode");

				if (!assertNotBlank(serviceName, opCode, ip, instance)) {
					continue;
				}

				serviceAndOpcodeSVImpl.localAccumulation(serviceName, opCode, elapsedTime);
				ServiceAndIpSVImpl.localAccumulation(serviceName, ip, elapsedTime);
				serviceAndInstanceSVImpl.localAccumulation(serviceName, instance, elapsedTime);

				serviceSVImpl.increment(serviceName);
				opcodeSVImpl.increment(opCode);

				long i = index.incrementAndGet();
				if (0 == i % 100000) {
					String now = DateFormatUtils.format(startTime, "yyyy-MM-dd HH:mm:ss");
					log.info(String.format("%s, 已处理: %12d", now, i));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 非空判断
	 *
	 * @param serviceName
	 * @param opCode
	 * @param ip
	 * @param instance
	 * @return
	 */
	private static final boolean assertNotBlank(String serviceName, String opCode, String ip, String instance) {

		if (StringUtils.isBlank(serviceName)) {
			return false;
		}

		if (StringUtils.isBlank(opCode)) {
			return false;
		}

		if (StringUtils.isBlank(ip)) {
			return false;
		}

		if (StringUtils.isBlank(instance)) {
			return false;
		}

		return true;
	}

}
