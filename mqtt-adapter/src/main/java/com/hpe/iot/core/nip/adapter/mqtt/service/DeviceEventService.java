package com.hpe.iot.core.nip.adapter.mqtt.service;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.iot.core.nip.adapter.data.IData;
import com.hpe.iot.core.nip.adapter.mqtt.bean.DemoRequest;
import com.hpe.iot.core.nip.adapter.mqtt.model.DeviceEvent;
import com.hpe.iot.core.nip.adapter.mqtt.model.KafkaDeviceEvent;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttRequest;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttResponse;
import com.hpe.iot.core.nip.adapter.mqtt.publisher.DcPublisher;
import com.hpe.iot.core.nip.adapter.mqtt.redis.model.RedisDeviceCache;
import com.hpe.iot.core.nip.adapter.mqtt.redis.model.RedisDeviceEvent;
import com.hpe.iot.core.nip.adapter.parser.IDataParser;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * DeviceEventService.
 */
@Service
@Slf4j
public class DeviceEventService {

	@Value("${spring.kafka.producer.normal-topic}")
	private String normalTopic;
	@Value("${spring.kafka.producer.prior-topic}")
	private String priorTopic;
	@Value("${spring.redis.event.expire}")
	private int expire;

	@Autowired
	private KafkaTemplate kafkaTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	@Qualifier("JsonParser")
	private IDataParser parser;
	// StringRedisTemplate只能对key=String，value=String的键值对进行操作，RedisTemplate可以对任何类型的key-value键值对操作。
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Value("${spring.mqtt.puburl}")
	private String puburl;

	@Autowired
	DcPublisher<?, ?> publisher;

	/**
	 * sendMessage.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @return boolean
	 */
	public boolean sendMessage(String clientId, MqttRequest message) {
		boolean ret = false;
		try {
			IData data = parser.parseData(clientId, message);
			if (data != null && data.getId() != null) {
				// if (cacheStringRedisTemplate.hasKey(data.getId())) {
				if (redisTemplate.hasKey(data.getId())) {
					RedisDeviceCache deviceCache = null;
					String value = redisTemplate.opsForValue().get(data.getId());
					try {
						deviceCache = objectMapper.readValue(value, RedisDeviceCache.class);
					} catch (IOException e) {
						log.error("Data conversion error. Data:[{}]", value);
					}
					if (deviceCache != null) {
						((KafkaDeviceEvent) data).setId(deviceCache.getId());
						String topic = normalTopic;
						if (this.isPriorMessage(message, data)) {
							topic = priorTopic;
						}
						String kfmessage = objectMapper.writeValueAsString(data);
						log.debug("kafka topic = {}, message = {}", topic, kfmessage);
						// コールバックを設定しない非同期呼び出し
						// kafkaTemplate.send(topic, kfmessage);

						// コールバックを設定する非同期呼び出し
						kafkaTemplate.send(topic, kfmessage)
								.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
									@Override
									public void onSuccess(SendResult<String, String> result) {
										// 成功時の処理
										log.info("Send Succcessful!");
									}

									@Override
									public void onFailure(Throwable ex) {
										// 失敗時の処理
										log.error("Failed send message!", ex.getMessage());
									}
								});

						// 同期呼び出し
						// kafkaTemplate.send(topic, kfmessage).get();

						ret = true;
					}
				} else {
					log.error("Device not registered.[device id:{}]", data.getId());
				}
			}
		} catch (Exception ex) {
			log.error("SendMessage Error.", ex);
		}
		return ret;
	}

	/**
	 * isPriorMessage.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param message
	 *            IData
	 * @return boolean
	 */
	public boolean isPriorMessage(MqttRequest request, IData message) {
		return false;
	}

  /**
   * receiveMessage.
   * https://www.codeflow.site/ja/article/spring-kafka
   * @throws Exception 
   */
	@KafkaListener(topics = "${spring.kafka.consumer.normal-topic}",
			       errorHandler = "consumerAwareErrorHandler")
	public void receiveMessage(String message) throws Exception {
		// TODO setBatchListener(true);
		// receive message by kafka
		log.debug("Topic:aeris-topic.receive message:[{}]", message);

		KafkaDeviceEvent kafkaDeviceEvent = objectMapper.readValue(message, KafkaDeviceEvent.class);
		boolean add = false;
		// check device id
		String id = kafkaDeviceEvent.getId();
		// message type
		String type = kafkaDeviceEvent.getType();
		if(type.equals("command")){
			MqttRequest mqttRequest = new MqttRequest();
			// create mqtt message
			mqttRequest.setMessageType(kafkaDeviceEvent.getType());
			mqttRequest.setRequestBody(kafkaDeviceEvent.getPayload());
			mqttRequest.setPush_session_id(UUID.randomUUID().toString());
			
	        // send to device
			log.info("Send to topic:/ip_push/req/" + id);
			MqttResponse mqtt_resp = publisher.sendRequestToDevice(puburl, "/ip_push/req/" + id, mqttRequest);
			log.info("Send Successful!");
		}else{
			if (redisTemplate.hasKey(id)) {
				String str = redisTemplate.opsForValue().get(id);
				RedisDeviceEvent redisDeviceEvent = objectMapper.readValue(str, RedisDeviceEvent.class);
				// check event timestamp
				if (kafkaDeviceEvent.getTimestamp() > redisDeviceEvent.getTimestamp()) {
					add = true;
				}
			} else {
				add = true;
			}
			// add event to redis
			if (add) {
				long now = System.currentTimeMillis();
				RedisDeviceEvent redisDeviceEvent = new RedisDeviceEvent();
				redisDeviceEvent.setId(id);
				redisDeviceEvent.setPayload(kafkaDeviceEvent.getPayload());
				redisDeviceEvent.setTimestamp(now);
				String str = objectMapper.writeValueAsString(redisDeviceEvent);
				redisTemplate.opsForValue().set(id, str);
				redisTemplate.expire(id, expire, TimeUnit.SECONDS);
				log.debug("save event to redis:[{}]", str);
			}
		}
	}

	@Bean
	public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
		return new ConsumerAwareListenerErrorHandler() {

			@Override
			public Object handleError(Message<?> message, ListenerExecutionFailedException e, Consumer<?, ?> consumer) {
				log.info("consumerAwareErrorHandler receive : " + message.getPayload().toString());
				return null;
			}
		};
	}

	/**
	 * getDeviceEvent.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param id
	 *            String
	 * @return DeviceEvent
	 */
	public DeviceEvent getDeviceEvent(HttpServletRequest request, String id) {
		DeviceEvent deviceEvent = null;
		if (redisTemplate.hasKey(id)) {
			String str = redisTemplate.opsForValue().get(id);
			try {
				RedisDeviceEvent redisDeviceEvent = objectMapper.readValue(str, RedisDeviceEvent.class);
				redisTemplate.delete(id);
				DeviceEvent.Payload payload = objectMapper.readValue(redisDeviceEvent.getPayload(),
						DeviceEvent.Payload.class);

				deviceEvent = new DeviceEvent();
				deviceEvent.setPayload(payload);

				DeviceEvent.Header header = new DeviceEvent.Header();
				header.setPayLoadVersion("1");
				header.setDeviceId(redisDeviceEvent.getId());
				header.setMessageId(UUID.randomUUID().toString());
				deviceEvent.setHeader(header);

			} catch (IOException ex) {
				deviceEvent = null;
				log.error("Data conversion error.", ex);
			}
		}
		return deviceEvent;
	}
}
