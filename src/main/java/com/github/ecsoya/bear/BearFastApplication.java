package com.github.ecsoya.bear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author angryred
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class BearFastApplication {
	public static void main(String[] args) {
		// System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(BearFastApplication.class, args);
	}
}
