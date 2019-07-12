package com.hpe.iot.core.nip.adapter.mqtt.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.iot.core.nip.adapter.mqtt.bean.MessageStatusType;
import com.hpe.iot.core.nip.adapter.mqtt.bean.MessageType;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnection;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnections;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnections.POOL_TYPE;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttMessageProcessable;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttRequest;
import com.hpe.iot.core.nip.adapter.mqtt.model.MqttResponse;
import com.hpe.iot.core.nip.adapter.mqtt.publisher.DcPublisher;
import com.hpe.iot.core.nip.adapter.mqtt.subscriber.DcSubscriber;
import com.hpe.iot.core.nip.adapter.mqtt.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MqttClientService implements DcSubscriber<MqttRequest, MqttResponse>, DcPublisher<MqttRequest, MqttResponse>,MqttMessageProcessable {
	
    @Autowired
    @Qualifier("subscribeThreadPool")
    private ExecutorService threadPool;
    
    @Autowired
    private DeviceEventService devService;

	@Value("${spring.mqtt.url}")
	private String[] brokerUrls;

	@Value("${spring.mqtt.topics}")
	private String[] subscribeTopics;

	@PostConstruct
	private void initMqttConnection() {
		try {
		    MqttConnections.initialize();
		for(String brokerUrl : brokerUrls) {
		    MqttConnection conn = MqttConnections.getMqttConnections(POOL_TYPE.SUB).getConnectionTo(brokerUrl);
		    conn.connect();
		    String topicArg = "";
            for (String topic : subscribeTopics) { topicArg += topic + " "; }
			log.info("Topic Subscribered. : " + brokerUrl + "topicArg :" + topicArg);
            conn.subscribe(subscribeTopics, this);
		}
		} catch (MqttException e) {
			log.error("Mqtt Client failed to connenct. : " + e.getMessage());
			System.exit(-1);
        }
	}
	
	@PreDestroy
	private void closeMqttConnection() {
		try {
		    MqttConnections.releaseAll();
		} catch (MqttException e) {
			log.error("Mqtt connection can not close. : " + e.getMessage());
		}
	}
	
	@Override
	public boolean process(String topic, int id, int qos, byte[] payload) {
		MqttConnection conn = MqttConnection.getCurrentConn();
	    
	    Runnable action = new Runnable() {
            
            @Override
            public void run() {
            	MqttConnection.setCurrentConn(conn);
            	log.info("Message Received. Topic : " + topic);
            	log.info("Message Received. Message : " + StringUtil.toString(payload));
                try {
                    if("req".equals(topic.split("/")[2])){
                        receiveRequestFromDevice(topic, getIpPushSpecificRequest(StringUtil.toString(payload)));
                    } else if("resp".equals(topic.split("/")[2])){
                        receiveResponseFromDevice(topic, getIpPushSpecificResponse(StringUtil.toString(payload)));
                    }
                } catch (Exception e){
                    log.error(e.getMessage());
                } finally {
                	MqttConnection.setCurrentConn(null);
                }
                
            }
        };
        
        threadPool.execute(action);
		
		
		return true;		
	}

	@Override
	public boolean supports(String topic) {
        return true;
	}

	private MqttRequest getIpPushSpecificRequest(String message) throws JsonParseException, JsonMappingException, IOException {
		MqttRequest mqttReq = new MqttRequest();
		mqttReq.setMessageType(getMsgTypeFromIpPushSpecificMessage(message));
		mqttReq.setRequestBody(message);
		return mqttReq;
	}
	
	private MqttResponse getIpPushSpecificResponse(String message) throws JsonParseException, JsonMappingException, IOException {
		MqttResponse mqttRes = new MqttResponse();
		mqttRes.setMessageType(getMsgTypeFromIpPushSpecificMessage(message));
		mqttRes.setResponseBody(message);
		return mqttRes;
	}
	
	private String getMsgTypeFromIpPushSpecificMessage(String message) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = (JsonNode) mapper.readValue(message, JsonNode.class);
		return rootNode.findValue("msg_type").asText();
	}

	@Override
	public void receiveResponseFromDevice(String respTopic, MqttResponse response) {
		
		response.setTopic(respTopic);
		
		try {
			if ("test".equals(response.getMessageType())) {}

		} catch (Exception e) {
		}
		
	}

	@Override
	public MqttResponse receiveRequestFromDevice(String reqTopic, MqttRequest request) {
		// get clientid from topic
		String clientId = reqTopic.split("/")[3];
		
		if (MessageType.NOTI_ERR.equals(request.getMessageType())) {
			devService.sendMessage(clientId, request);
		}else if (MessageType.NOTI_NORM.equals(request.getMessageType())) {
			devService.sendMessage(clientId, request);
		}
		
		return null;
	}

//	@Override
//	public MqttResponse sendRequestToDevice(MqttRequest request) {
//	MqttResponse mqttResponse = new MqttResponse();
//	
//	try {
//		String topic = request.getTopic();
//		MqttConnection conn = MqttConnection.getCurrentConn();
//		conn.publish(topic, request.getRequestBody().getBytes());
//		mqttResponse.setMessageStatus(MessageStatusType.CLIENT_REACHING);
//	} catch (MqttException e) {
//		mqttResponse.setMessageStatus(MessageStatusType.CLIENT_NOT_REACHED);
//		logger.error("Unable to send the Commands to the device. " + e.getMessage());
//		logger.error("000018E", e);
//	}
//
//	return mqttResponse;
//	}
    private ObjectMapper mapper = new ObjectMapper();

	@Override
	public MqttResponse sendRequestToDevice(String brokerUrl, String topic, MqttRequest request) {
		MqttResponse mqttResponse = new MqttResponse();
        try {
            String message = mapper.writeValueAsString(request);
            MqttConnection connection = MqttConnections.getMqttConnections(POOL_TYPE.PUB)
                    .getConnectionTo(brokerUrl);
            if (!connection.isConnected()) {
                connection.connect();
            }

            connection.publish(topic, StringUtil.toBytes(message));
            mqttResponse.setMessageStatus(MessageStatusType.CLIENT_REACHING);
        } catch (Exception e) {
//            logger.debug(e.getMessage(), e);
        	System.out.println(e.getStackTrace());
    		mqttResponse.setMessageStatus(MessageStatusType.CLIENT_NOT_REACHED);
        }
    	return mqttResponse;
    }

	@Override
	public void sendResponseToDevice(MqttResponse response) {
		// TODO Auto-generated method stub
		
	}

	
}