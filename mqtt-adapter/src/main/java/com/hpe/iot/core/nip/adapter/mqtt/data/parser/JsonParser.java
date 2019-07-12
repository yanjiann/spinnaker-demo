package com.hpe.iot.core.nip.adapter.mqtt.data.parser;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.iot.core.nip.adapter.data.IData;
import com.hpe.iot.core.nip.adapter.mqtt.model.DeviceEvent;
import com.hpe.iot.core.nip.adapter.mqtt.model.KafkaDeviceEvent;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttRequest;
import com.hpe.iot.core.nip.adapter.parser.IDataParser;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JsonParser.
 */
@Slf4j
@Component("JsonParser")
public class JsonParser implements IDataParser {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public IData parseData(HttpServletRequest request) {
		return null;
	}

	@Override
	public IData parseData(String clientId, Object request) {
		KafkaDeviceEvent data = new KafkaDeviceEvent();
		try {
//			DeviceEvent event = objectMapper.readValue(request, DeviceEvent.class);
//			log.debug("Request Data  = {}", event.toString());
//			data.setId(event.getHeader().getDeviceId());'
			data.setId(clientId);
			data.setPayload(objectMapper.writeValueAsString(request));
			data.setTimestamp(System.currentTimeMillis());
		} catch (Exception ex) {
			log.error("Data pares error.", ex);
			data = null;
		}
		return data;
	}
}
