package com.globalmed.mes.mes_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MesApiApplication.class, args);
	}

}
