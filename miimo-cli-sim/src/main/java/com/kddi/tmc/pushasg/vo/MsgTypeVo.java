package com.kddi.tmc.pushasg.vo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.kddi.tmc.pushasg.common.vo.BaseJsonVo;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsgTypeVo extends BaseJsonVo {
    private static final Logger logger = LoggerFactory.getLogger(MsgTypeVo.class);
    
    public static final String MSG_TYPE_ALLOC_REQ = "alloc_req";
    
    public static final String MSG_TYPE_ALLOC_RESP = "alloc_resp";
    
    public static final String MSG_TYPE_REG_CLIENT = "reg_client";
    
    public static final String MSG_TYPE_REG_CLIENT_ACK = "reg_client_ack";
    
    public static final String MSG_TYPE_PUSH_MESSAGE = "push_message";
    
    public static final String MSG_TYPE_PUSH_MESSAGE_ACK = "push_message_ack";
    
    public static final String MSG_TYPE_PUSH_RESULT = "push_result";
    
    public static final String MSG_TYPE_PUSH_RESULT_ACK = "push_result_ack";
    
    public static final String MSG_TYPE_RESEND_REQ = "resend_req";
    
    public static final String MSG_TYPE_RESEND_REQ_ACK = "resend_req_ack";
    
    public static final String MSG_TYPE_GROUP_UPDATE_REQ = "g_update";
    
    public static final String MSG_TYPE_GROUP_UPDATE_ACK = "g_update_ack";
    
//    public static final String MSG_TYPE_PUSH_OFF_REQ = "push_off";
//    
//    public static final String MSG_TYPE_PUSH_OFF_ACK = "push_off_ack";
    
    public static final String MSG_TYPE_APP_UPD_REQ = "a_update";
    
    public static final String MSG_TYPE_APP_UPD_ACK = "a_update_ack";
    
    @JsonProperty("msg_type")
    @NonNull
    private String msgType;
    
    /**
     * JSONシリアライズされたMsgTypeVoをparseしてインスタンスを生成するスタティックファクトリメソッド
     * 
     * @param jsonRequest
     *            JSONシリアライズされたResponse
     * @return jsonRequestをparseして得たインスタンスを返す。
     *         jsonRequestが不正などでparseに失敗した場合はnullを返す。
     */
    public static MsgTypeVo valueOf(String jsonRequest) {
        try {
            return mapper.readValue(jsonRequest, MsgTypeVo.class);
        } catch (JsonParseException e) {
            logger.warn("Received JSON format is invalid.");
            return null;
        } catch (JsonMappingException e) {
            logger.warn("Received Resource does not conform to the terms of TMC Push.");
            logger.warn(e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
