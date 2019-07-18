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
public class CommandResponseVo extends BaseJsonVo {
    private static final Logger logger = LoggerFactory.getLogger(CommandResponseVo.class);

    @JsonProperty("msg_type")
    @NonNull
    private String msgType;

    @JsonProperty("msg_id")
    @NonNull
    private String msgId;

    @JsonProperty("event_time")
    @NonNull
    private EventTimeVo eventTime;
    
    @JsonProperty("result")
    @NonNull
    private String result;

    @JsonProperty("err_msg")
    private String errMsg;
    
    /**
     * JSONシリアライズされたRequestPrimitiveをparseしてインスタンスを生成するスタティックファクトリメソッド
     * 
     * @param jsonRequest
     *            JSONシリアライズされたResponse
     * @return jsonRequestをparseして得たインスタンスを返す。
     *         jsonRequestが不正などでparseに失敗した場合はnullを返す。
     */
    public static CommandResponseVo valueOf(String jsonRequest) {
        try {
            return mapper.readValue(jsonRequest, CommandResponseVo.class);
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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventTimeVo {

        @JsonProperty("req_time")
        @NonNull
        private Float reqTime;

        @JsonProperty("resp_time")
        @NonNull
        private Float respTime;
    }

}
