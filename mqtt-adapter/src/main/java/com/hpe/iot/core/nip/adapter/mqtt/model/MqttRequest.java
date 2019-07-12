/*
* Copyright 2015 Hewlett-Packard Co. All Rights Reserved.
* An unpublished and CONFIDENTIAL work. Reproduction,
* adaptation, or translation without prior written permission
* is prohibited except as allowed under the copyright laws.
*-----------------------------------------------------------------------------
* Project: IOT
* Module:  IOT
* Package: com.hpe.iot.dc.south.model
* Source:  Mqttrequest.java
* Author:  P M, Shakir
* Organization: HP 
 * Revision: 1.0
* Date: Mar 7, 2016
* Contents:
*-----------------------------------------------------------------------------
*/

package com.hpe.iot.core.nip.adapter.mqtt.model;

public class MqttRequest {

//	String topic;
//	int qos;
	String requestBody;
	String messageType;
//	String messageStatus;
	String push_session_id;
	public String getPush_session_id() {
		return push_session_id;
	}

	public void setPush_session_id(String push_session_id) {
		this.push_session_id = push_session_id;
	}

	public Boolean getCheck_app_reached() {
		return check_app_reached;
	}

	public void setCheck_app_reached(Boolean check_app_reached) {
		this.check_app_reached = check_app_reached;
	}

	public Boolean check_app_reached;

//	public String getTopic() {
//		return topic;
//	}
//
//	public void setTopic(String topic) {
//		this.topic = topic;
//	}
//
//	public int getQos() {
//		return qos;
//	}
//
//	public void setQos(int qos) {
//		this.qos = qos;
//	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	@Override
	public String toString() {
		return "MqttRequest [requestBody=" + requestBody + ", messageType=" + messageType + ", push_session_id="
				+ push_session_id + ", check_app_reached=" + check_app_reached + "]";
	}

//	public String getMessageStatus() {
//		return messageStatus;
//	}
//
//	public void setMessageStatus(String messageStatus) {
//		this.messageStatus = messageStatus;
//	}

//	@Override
//	public String toString() {
//		return "MqttRequest [topic=" + topic + ", qos=" + qos + ", requestBody=" + requestBody + "]";
//	}

}
