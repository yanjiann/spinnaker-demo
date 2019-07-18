package com.kddi.tmc.pushasg.common.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseJsonVo {

    private static final Logger logger = LoggerFactory
            .getLogger(BaseJsonVo.class);
    protected static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * @return JSONシリアライズされたインスタンス
     */
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.debug(e.getMessage(), e);
            return null;
        }
    }

}
