package com.ai.risk.analysis.modules.warning.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

/**
 * LOG4X-TRACE-TOPIC 消费者
 * <p>
 * 使用 @KafkaListener 注解, 可以指定: 主题, 分区, 消费组
 *
 * @author Steven
 * @date 2019-05-24
 */
@Slf4j
@Component
public class L4xTraceConsumer {

	/**
	 * 链路 Topic
	 */
	private static final String TOPIC = "LOG4X-TRACE-TOPIC";

	/**
	 * 是否为主服务，用于快速判断
	 */
	private static final String CALLTYPE_CSF = "\"callType\":\"CSF\"";

	/**
	 * 是否为 JDBC 调用信息，用于快速判断
	 */
	private static final String CALLTYPE_JDBC = "\"callType\":\"JDBC\"";

	/**
	 * 本地数据聚合
	 *
	 * @param record
	 */
	private void accumulation(ConsumerRecord<?, String> record, int partition) {

		String value = record.value();
		if (value.indexOf(CALLTYPE_CSF) != -1) {
			Queue.offerService(value);
		}

		if (value.indexOf(CALLTYPE_JDBC) != -1) {
			Queue.offerJdbc(value);
		}

	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "0")})
	public void listen0(ConsumerRecord<?, String> record) {
		accumulation(record, 0);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "1")})
	public void listen1(ConsumerRecord<?, String> record) {
		accumulation(record, 1);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "2")})
	public void listen2(ConsumerRecord<?, String> record) {
		accumulation(record, 2);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "3")})
	public void listen3(ConsumerRecord<?, String> record) {
		accumulation(record, 3);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "4")})
	public void listen4(ConsumerRecord<?, String> record) {
		accumulation(record, 4);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "5")})
	public void listen5(ConsumerRecord<?, String> record) {
		accumulation(record, 5);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "6")})
	public void listen6(ConsumerRecord<?, String> record) {
		accumulation(record, 6);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "7")})
	public void listen7(ConsumerRecord<?, String> record) {
		accumulation(record, 7);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "8")})
	public void listen8(ConsumerRecord<?, String> record) {
		accumulation(record, 8);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "9")})
	public void listen9(ConsumerRecord<?, String> record) {
		accumulation(record, 9);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "10")})
	public void listen10(ConsumerRecord<?, String> record) {
		accumulation(record, 10);
	}

	@KafkaListener(topicPartitions = {@TopicPartition(topic = TOPIC, partitions = "11")})
	public void listen11(ConsumerRecord<?, String> record) {
		accumulation(record, 11);
	}

}
