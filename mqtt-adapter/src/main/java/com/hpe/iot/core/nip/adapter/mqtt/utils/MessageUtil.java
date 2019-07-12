package com.hpe.iot.core.nip.adapter.mqtt.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageUtil
{
	private final static ResourceBundle resourceBundle;
	
	
    public static final String MSGID_UNKOWN_REQ = "000001W";
    public static final String MSGID_PARAM_ERROR_REQUIRED = "000004W";
    public static final String MSGID_PARAM_ERROR_STYLE = "000005W";
    public static final String MSGID_PARAM_ERROR_UNNECESSARY = "000006W";
    public static final String MSGID_PARAM_ERROR_MISMATCH = "000007W";
    public static final String MSGID_PARAM_BUSINESS_CD_AUTH_ERROR = "000008W";
    public static final String MSGID_DEVICE_UNIQ_ID_AUTH_ERROR = "000009W";
    public static final String MSGID_UNKOWN_EXCEPTION = "000018E";
    
    public static final String MSGID_ID_MNT_SEND_REQ = "002001D";
    public static final String MSGID_ID_MNT_RECIEVE_RES = "002002D";
    public static final String MSGID_ID_MNT_COMMU_FAIL = "002003E";
    public static final String MSGID_ID_MNT_RETRIEVE_FAIL = "002004E";
    public static final String MSGID_ID_MNT_CREATE_FAIL = "002005E";
    public static final String MSGID_ID_MNT_UPDATE_FAIL = "002006E";
    public static final String MSGID_ID_MNT_DELETE_FAIL = "002007E";
    
    public static final String MSGID_MQTT_CONN_SUCCESS = "001001I";
    public static final String MSGID_MQTT_CONN_CLOSED = "001002I";
    public static final String MSGID_MQTT_DISCONN = "001003E";
    public static final String MSGID_MQTT_RECONN = "001004E";
    public static final String MSGID_MQTT_RECONN_SUCCESS = "001005I";
    public static final String MSGID_MQTT_SUBSCRIBE_SUCCESS = "001006I";
    
    public static final String MSG_PROCESS_ERROR="Push server internal error: {0}";

	static{
		resourceBundle = ResourceBundle.getBundle("logmessage");
	}

	public static String getMessage(String id, Object[] param)
	{
		String pattern =  resourceBundle.getString(id);
		return MessageFormat.format(pattern, param);
		
	}
	
	public static String getMessage(String id)
	{
		String pattern = resourceBundle.getString(id);
		return pattern;
	}
	
	public static String getProcessError(Object[] param){
		return MessageFormat.format(MSG_PROCESS_ERROR,param);
	}
}