package com.finance_control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.finance_control"})
public class FinanceControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceControlApplication.class, args);
	}

}
