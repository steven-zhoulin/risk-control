package com.ai.risk.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Steven
 * @date 2019-05-26
 */
@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}

}
