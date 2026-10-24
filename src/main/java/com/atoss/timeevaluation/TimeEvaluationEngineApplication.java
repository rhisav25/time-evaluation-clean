package com.atoss.timeevaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TimeEvaluationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeEvaluationEngineApplication.class, args);
	}

}
