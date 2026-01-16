package com.workforce.wms;

import org.springframework.boot.SpringApplication;

public class TestWmsApplication {

	public static void main(String[] args) {
		SpringApplication.from(WmsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
