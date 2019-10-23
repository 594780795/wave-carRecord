package com.wave.carRecord;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class CarRecordApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarRecordApplication.class, args);
	}

}
