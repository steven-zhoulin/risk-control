package com.ai.risk.analysis.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven
 */
@Configuration
@ComponentScan(basePackages = {"com.ai.risk.analysis.modules.warning.kafka.thread"})
public class ThreadConfig {

}
