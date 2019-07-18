package com.kddi.tmc.pushasg.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public final class MessageUtil {
    public static final String MSGID_ALLOC_START = "201401I";
    public static final String MSGID_ALLOC_END = "201402I";
    public static final String MSGID_ALLOC_SUCCESS = "201404I";
    public static final String MSGID_ALLOC_RET_SUCCESS = "201405I";
    public static final String MSGID_SERVER_BLOCKED = "201406I";
    public static final String MSGID_BROKER_ALLOC_FAIL = "201407E";
    public static final String MSGID_PUSH_ALLOC_FAIL = "201408E";
    public static final String MSGID_BROKER_OVER_THRESHOLD = "201409E";
    public static final String MSGID_PUSH_OVER_THRESHOLD = "201410E";

    public static final String MSGID_UNKOWN_REQ = "000001W";
    public static final String MSGID_PARAM_ERROR_REQUIRED = "000004W";
    public static final String MSGID_PARAM_ERROR_STYLE = "000005W";
    public static final String MSGID_PARAM_ERROR_UNNECESSARY = "000006W";
    public static final String MSGID_PARAM_ERROR_MISMATCH = "000007W";
    public static final String MSGID_PARAM_BUSINESS_CD_AUTH_ERROR = "000008W";
    public static final String MSGID_DEVICE_UNIQ_ID_AUTH_ERROR = "000009W";
    public static final String MSGID_UNKOWN_EXCEPTION = "000018E";
    

    public static final String MSGID_MQTT_CONN_SUCCESS = "001001I";
    public static final String MSGID_MQTT_CONN_CLOSED = "001002I";
    public static final String MSGID_MQTT_DISCONN = "001003E";
    public static final String MSGID_MQTT_RECONN = "001004E";
    public static final String MSGID_MQTT_RECONN_SUCCESS = "001005I";
    public static final String MSGID_MQTT_SUBSCRIBE_SUCCESS = "001006I";

    public static final String MSGID_ID_MNT_SEND_REQ = "002001D";
    public static final String MSGID_ID_MNT_RECIEVE_RES = "002002D";
    public static final String MSGID_ID_MNT_COMMU_FAIL = "002003E";
    public static final String MSGID_ID_MNT_RETRIEVE_FAIL = "002004E";
    public static final String MSGID_ID_MNT_CREATE_FAIL = "002005E";
    public static final String MSGID_ID_MNT_UPDATE_FAIL = "002006E";
    public static final String MSGID_ID_MNT_DELETE_FAIL = "002007E";

    private static final Properties messages = new Properties();
    private static final String PROP_FILE = "/logmessage.properties";

    static {
        InputStream is = MessageUtil.class.getResourceAsStream(PROP_FILE);
        try {
            messages.load(is);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getMessage(String msgId, Object... args) {
        String message = messages.getProperty(msgId, "");
        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }

        return message;
    }
}
