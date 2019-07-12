/*
* Copyright 2015 Hewlett-Packard Co. All Rights Reserved.
* An unpublished and CONFIDENTIAL work. Reproduction,
* adaptation, or translation without prior written permission
* is prohibited except as allowed under the copyright laws.
*-----------------------------------------------------------------------------
* Project: IOT
* Module:  IOT
* Package: com.hpe.iot.dc.service
* Source:  DcSubscriber.java
* Author:  P M, Shakir
* Organization: HP 
 * Revision: 1.0
* Date: Mar 7, 2016
* Contents:
*-----------------------------------------------------------------------------
*/

package com.hpe.iot.core.nip.adapter.mqtt.subscriber;

public interface DcSubscriber <Req, Res>{
	
	/**
	 * @param request
	 * @return
	 */
	public Res receiveRequestFromDevice(String reqTopic, Req request);
	/**
	 * @param response
	 */
	public void receiveResponseFromDevice(String respTopic, Res response);

}
