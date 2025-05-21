package com.backend.demo;

import com.backend.demo.service.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
	private static final int CITIES_TO_GENERATE = 200;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner initializeDatabase(@Autowired CityService cityService) {
		return args -> {
			try {
				cityService.generateCities(CITIES_TO_GENERATE);
				logger.info("Completed city generation");

			} catch (Exception e) {
				logger.error("Error during automatic city generation: {}", e.getMessage(), e);
			}
		};
	}
}
