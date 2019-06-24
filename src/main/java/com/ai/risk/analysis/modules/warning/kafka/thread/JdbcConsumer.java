package com.ai.risk.analysis.modules.warning.kafka.thread;

import com.ai.risk.analysis.modules.warning.kafka.Queue;
import com.ai.risk.analysis.modules.warning.service.impl.JdbcSVImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Steven
 */
@Slf4j
@Component
public class JdbcConsumer extends Thread {

	private ObjectMapper mapper = new ObjectMapper();

	private AtomicLong index = new AtomicLong(0L);

	@Autowired
	private JdbcSVImpl jdbcSVImpl;

	@Override
	@PostConstruct
	public void start() {
		super.start();
		log.info("JdbcConsumer started...");
	}

	@Override
	public void run() {

		while (true) {
			String value = Queue.pollJdbc();
			if (null == value) {
				continue;
			}

			Map span = null;
			try {
				span = mapper.readValue(value, Map.class);
			} catch (IOException e) {

			}

			String callType = (String) span.get("callType");
			if (!"JDBC".equals(callType)) {
				return;
			}

			Long startTime = (Long) span.get("startTime");
			Integer elapsedTime = (Integer) span.get("elapsedTime");
			Map specMap = (Map) span.get("spec");
			String sql = (String) specMap.get("sql");
			String dsName = (String) specMap.get("dsName");

			sql = StringUtils.strip(sql).toUpperCase();

			int blankIndex = findBlank(sql);
			String crud = sql.substring(0, blankIndex);

			String tableName = null;
			if ("INSERT".equals(crud)) {
				tableName = fetchTableNameFromInsertSQL(sql);
				if (StringUtils.isNotBlank(tableName)) {
					jdbcSVImpl.localAccumulation(tableName, "insert", elapsedTime);
				}
			} else if ("UPDATE".equals(crud)) {
				tableName = fetchTableNameFromUpdateSQL(sql);
				if (StringUtils.isNotBlank(tableName)) {
					jdbcSVImpl.localAccumulation(tableName, "update", elapsedTime);
				}
			} else if ("DELETE".equals(crud)) {
				tableName = fetchTableNameFromDeleteSQL(sql);
				if (StringUtils.isNotBlank(tableName)) {
					jdbcSVImpl.localAccumulation(tableName, "delete", elapsedTime);
				}
			} else if ("SELECT".equals(crud)){
				tableName = fetchTableNameFromSelectSQL(sql);
				if (StringUtils.isNotBlank(tableName)) {
					jdbcSVImpl.localAccumulation(tableName, "select", elapsedTime);
				}
			}

			long i = index.incrementAndGet();
			if (0 == i % 1000000) {
				String now = DateFormatUtils.format(startTime, "yyyy-MM-dd HH:mm:ss");
				log.info(String.format("%s, 已处理: %12d", now, i));
			}
		}
	}

	private int findBlank(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!isVariableChar(c)) {
				return i;
			}
		}
		return -1;
	}

	private String fetchTableNameFromSelectSQL(String sql) {
		int index = sql.indexOf("FROM");
		if (-1 != index) {
			sql = StringUtils.removeStart(sql.substring(index), "FROM");
			return fetchTableName(sql);
		} else {
			return null;
		}
	}

	/**
	 * INSERT INTO TABLE_NAME
	 *
	 * @param sql
	 * @return
	 */
	private String fetchTableNameFromInsertSQL(String sql) {
		int index = sql.indexOf("INTO");
		if (-1 != index) {
			sql = StringUtils.removeStart(sql.substring(index), "INTO");
			return fetchTableName(sql);
		}
		return null;
	}

	/**
	 * UPDATE TABLE_NAME
	 * @param sql
	 * @return
	 */
	private String fetchTableNameFromUpdateSQL(String sql) {
		sql = StringUtils.removeStart(sql, "UPDATE");
		return fetchTableName(sql);
	}

	/**
	 * DELETE TABLE_NAME
	 * DELETE FROM TABLE_NAME
	 *
	 * @param sql
	 * @return
	 */
	private String fetchTableNameFromDeleteSQL(String sql) {
		int index = sql.indexOf("FROM");
		if (-1 != index) {
			sql = StringUtils.removeStart(sql.substring(index), "FROM");
			return fetchTableName(sql);
		} else {
			sql = StringUtils.removeStart(sql, "DELETE");
			return fetchTableName(sql);
		}

	}

	private String fetchTableName(String sql) {
		sql = StringUtils.strip(sql);

		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (!isVariableChar(c)) {
				return sql.substring(0, i);
			}
		}

		return null;
	}


	public static final boolean isVariableChar(char c) {
		if (('0' <= c) && (c <= '9')) {
			return true;
		}
		if (('A' <= c) && (c <= 'Z')) {
			return true;
		}
		if (('a' <= c) && (c <= 'z')) {
			return true;
		}
		return '_' == c;
	}

}
