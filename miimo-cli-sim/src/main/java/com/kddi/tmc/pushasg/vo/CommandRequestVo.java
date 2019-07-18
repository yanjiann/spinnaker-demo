package com.kddi.tmc.pushasg.vo;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.kddi.tmc.pushasg.common.vo.BaseJsonVo;


@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandRequestVo extends BaseJsonVo {
    private static final Logger logger = LoggerFactory.getLogger(CommandRequestVo.class);

    @JsonProperty("messageType")
    //@NonNull
    private String msgType;

    @JsonProperty("client_id")
    //@NonNull
    private String clientId;

    @JsonProperty("message")
    //@NonNull
    private String message;

    @JsonProperty("event_time")
    private EventTimeVo eventTime;
    
    
    /**
     * JSONシリアライズされたRequestPrimitiveをparseしてインスタンスを生成するスタティックファクトリメソッド
     * 
     * @param jsonRequest
     *            JSONシリアライズされたResponse
     * @return jsonRequestをparseして得たインスタンスを返す。
     *         jsonRequestが不正などでparseに失敗した場合はnullを返す。
     */
    public static CommandRequestVo valueOf(String jsonRequest) {
        try {
            return mapper.readValue(jsonRequest, CommandRequestVo.class);
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
    }

}
