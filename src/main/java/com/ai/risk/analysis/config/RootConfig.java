package com.ai.risk.analysis.config;

import com.ai.risk.analysis.modules.warning.entity.unit.PointUnit;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Steven
 * @date 2019-05-24
 */
@Configuration
public class RootConfig {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public Map<String, PointUnit> localCounter() {
		return new ConcurrentHashMap(100000000);
	}

	@Bean
	public InfluxDB influxDB() {
		InfluxDB influxDB = InfluxDBFactory.connect("http://10.174.59.41:8086", "admin", "admin");
		String dbName = "hubble";
		influxDB.setDatabase(dbName);
		influxDB.enableBatch(BatchOptions.DEFAULTS.actions(5000).flushDuration(1000));
		influxDB.enableGzip();
		return influxDB;
	}

	@Autowired(required = false)
	public void setRedisTemplate(RedisTemplate redisTemplate) {
		RedisSerializer stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(stringSerializer);
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(stringSerializer);
	}
}
