package com.hpe.iot.core.nip.adapter.mqtt.model;

import java.util.List;

public abstract class Request extends BaseJsonVo{
	
	// used by checkRequestValidator
	public abstract boolean validateRequest(List<String> violationMsgList);
}
