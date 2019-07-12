package com.hpe.iot.core.nip.adapter.mqtt.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hpe.iot.core.nip.adapter.mqtt.utils.MessageUtil;
import com.hpe.iot.core.nip.adapter.mqtt.utils.StringUtil;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoRequest {
	
	
	public String msg_type = MessageType.DEV_START;
	
	@JsonProperty("client_id")
	@Size(max=64, min=1, message=MessageUtil.MSGID_PARAM_ERROR_STYLE + ":client_id")	
	public String client_id;

	@JsonProperty("check_app_reached")
	@NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":check_app_reached")	
	public Boolean check_app_reached;

	@JsonProperty("message")
	@NotNull(message=MessageUtil.MSGID_PARAM_ERROR_REQUIRED + ":message")	
	public String message;
	
	@JsonIgnore
    public String getTopic() {
        if (StringUtil.isNotEmpty(client_id)) {
            return "/ip_push/req/" + client_id;
        }
        return null;
    }

	@Override
	public String toString() {
		return "DemoRequest [msg_type=" + msg_type + ", client_id=" + client_id + ", check_app_reached="
				+ check_app_reached + ", message=" + message + "]";
	}
}
