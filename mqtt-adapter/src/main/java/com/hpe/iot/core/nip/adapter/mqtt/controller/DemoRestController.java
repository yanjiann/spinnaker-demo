package com.hpe.iot.core.nip.adapter.mqtt.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.iot.core.nip.adapter.mqtt.bean.DemoRequest;
import com.hpe.iot.core.nip.adapter.mqtt.bean.DemoResponse;
import com.hpe.iot.core.nip.adapter.mqtt.exception.ApplicationException;
import com.hpe.iot.core.nip.adapter.mqtt.model.DeviceEvent;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttRequest;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttResponse;
import com.hpe.iot.core.nip.adapter.mqtt.publisher.DcPublisher;
import com.hpe.iot.core.nip.adapter.mqtt.service.DeviceEventService;
import com.hpe.iot.core.nip.adapter.mqtt.validator.BeanValidationUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo Controller to Test Rest API
 * 
 * @author Moniruzzaman Md
 *
 */
@RestController
@Slf4j
public class DemoRestController {

	@Value("${spring.mqtt.puburl}")
	private String puburl;

	@Autowired
	DcPublisher<?, ?> publisher;

    private ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "/mqtt/send", method = RequestMethod.POST)
	public String index(HttpServletRequest req, HttpServletResponse res, @RequestBody String body) {
		
		DemoRequest demorequest = null;
		DemoResponse demoResponse = new DemoResponse();
		String responseBody = null;
		
		MqttRequest mqttRequest = new MqttRequest();
		try {
			demorequest = BeanValidationUtil.validateHttpRequest(body, DemoRequest.class);
			log.info(demorequest.toString());
			
			// create mqtt message
			mqttRequest.setMessageType(demorequest.msg_type);
			mqttRequest.setCheck_app_reached(demorequest.check_app_reached);
			mqttRequest.setRequestBody(demorequest.message);
			mqttRequest.setPush_session_id(demoResponse.push_session_id);
			
	        // send to device
	        //MqttResponse mqtt_resp = publisher.sendRequestToDevice("tcp://localhost:1883", demorequest.getTopic(), mqttRequest);
			MqttResponse mqtt_resp = publisher.sendRequestToDevice(puburl, demorequest.getTopic(), mqttRequest);

	        responseBody = mapper.writeValueAsString(demoResponse);
			log.info(responseBody);

		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseBody;
	}
}
