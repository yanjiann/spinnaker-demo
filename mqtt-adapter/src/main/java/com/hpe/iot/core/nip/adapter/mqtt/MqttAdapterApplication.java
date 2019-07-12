package com.hpe.iot.core.nip.adapter.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Spring Boot Initializer Class
 * 
 * @author HPE
 *
 */

@SpringBootApplication
@ComponentScan
//@EnableKafka
public class MqttAdapterApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MqttAdapterApplication.class, args);
	}

}
