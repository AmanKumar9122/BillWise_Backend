package com.aksps.BillWise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillWiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillWiseApplication.class, args);
	}

}

