package com.hpe.iot.core.nip.adapter.mqtt.conf;

import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HdKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException e) throws Exception {
		
		log.info("error handler for message: {} [{}], exception: {}", message.getPayload(), message.getHeaders(), e.getMessage());
		System.out.println(e.getMessage());
		return null;
	}
    

}
