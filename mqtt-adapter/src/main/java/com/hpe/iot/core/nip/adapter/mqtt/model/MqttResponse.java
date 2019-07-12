/*
* Copyright 2015 Hewlett-Packard Co. All Rights Reserved.
* An unpublished and CONFIDENTIAL work. Reproduction,
* adaptation, or translation without prior written permission
* is prohibited except as allowed under the copyright laws.
*-----------------------------------------------------------------------------
* Project: IOT
* Module:  IOT
* Package: com.hpe.iot.dc.south.model
* Source:  MqttResponse.java
* Author:  P M, Shakir
* Organization: HP 
 * Revision: 1.0
* Date: Mar 7, 2016
* Contents:
*-----------------------------------------------------------------------------
*/

package com.hpe.iot.core.nip.adapter.mqtt.model;


public class MqttResponse {

	String topic;
	int qos;
	String responseBody;
	String messageType;
	String messageStatus;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public void setTopicFromRequest(String requestTopic){
		this.topic = requestTopic.replaceFirst("/req/", "/resp/");
	}

	public int getQos() {
		return qos;
	}

	public void setQos(int qos) {
		this.qos = qos;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	@Override
	public String toString() {
		return "MqttResponse [topic=" + topic + ", qos=" + qos + ", responseBody=" + responseBody + "]";
	}
	
	public String getTopicPart(int position){
		String returnTopicPart = null;
		if(this.topic != null){
			String[] topicSplit = topic.split("/");
			if(topicSplit != null && topicSplit.length >= position){
				returnTopicPart = topicSplit[position];
			}
		}
		return returnTopicPart;		
	}

}
