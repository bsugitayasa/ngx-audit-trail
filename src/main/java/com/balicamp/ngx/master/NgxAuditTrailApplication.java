package com.balicamp.ngx.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@EnableAutoConfiguration
@EnableMongoRepositories
//@EnableJpaRepositories(basePackages = "com")
//@EntityScan(basePackages="com")
//@EnableTransactionManagement
//@ComponentScan("com")
public class NgxAuditTrailApplication {

	public static void main(String[] args) {
		SpringApplication.run(NgxAuditTrailApplication.class, args);
	}
}
