package com.finance_control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.finance_control"},
               excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX,
                                                     pattern = "com\\.finance_control\\.unit\\..*"))
public class FinanceControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceControlApplication.class, args);
    }

}
