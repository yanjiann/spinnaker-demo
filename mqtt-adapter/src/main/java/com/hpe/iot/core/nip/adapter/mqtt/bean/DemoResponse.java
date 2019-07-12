package com.hpe.iot.core.nip.adapter.mqtt.bean;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hpe.iot.core.nip.adapter.mqtt.utils.MessageUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoResponse {
    
	public DemoResponse(){
		setPushSessionId();
	}
	
	void setPushSessionId(){
		push_session_id = UUID.randomUUID().toString();
	}
	
    @NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":msg_type")
	public String msg_type;
    
    @NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":push_session_id")
    @Size(max=64, min=1, message=MessageUtil.MSGID_PARAM_ERROR_STYLE)
	public String push_session_id;
    
    @NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":client_id")
    @Size(max=64, min=1, message=MessageUtil.MSGID_PARAM_ERROR_STYLE)
	public String client_id;
    
    @NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":result")
	public String result;
	
	public String err_msg;

}
