package com.bu.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherMateApplication.class, args);
	}

}
