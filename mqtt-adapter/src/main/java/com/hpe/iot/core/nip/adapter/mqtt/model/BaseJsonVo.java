package com.hpe.iot.core.nip.adapter.mqtt.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hpe.iot.core.nip.adapter.mqtt.exception.ApplicationException;
import com.hpe.iot.core.nip.adapter.mqtt.validator.Constants;

public abstract class BaseJsonVo {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final ObjectMapper noRootMapper = new ObjectMapper();
    private static final HashMap<Class<?>, Object> valueOfMethods = new HashMap<Class<?>, Object>();
    private static final Object NO_METHOD = new Object();
    
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        noRootMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        //mapper.enable(SerializationFeature.w)
    }

    /**
     * @return JSONシリアライズされたインスタンス
     */
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            //logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * @return JSONシリアライズされたインスタンス
     */
    public String toJsonNoRoot() {
        try {
            return noRootMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            //logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    
    private static Method getValueOfMethod(Class<?> clazz) {
        Object method = valueOfMethods.get(clazz);
        if (method == null) {
            synchronized (valueOfMethods) {
                method = valueOfMethods.get(clazz);

                if (method == null) {
                    Method valueOfMethod = null;
                    try {
                        valueOfMethod = clazz.getDeclaredMethod("valueOf",
                                String.class);
                    } catch (Exception e1) {
                        valueOfMethod = null;
                    }

                    if (valueOfMethod == null) {
                        valueOfMethods.put(clazz, NO_METHOD);
                        method = NO_METHOD;
                    } else {
                        valueOfMethods.put(clazz, valueOfMethod);
                        method = valueOfMethod;
                    }
                }
            }
        }
        
        if (method == NO_METHOD) {
            return null;
        }
        
        return (Method) method;
    }
    
    
    /**
     * JSONシリアライズされたVoをparseしてインスタンスを生成するスタティックファクトリメソッド
     * 
     * @param jsonRequest
     *            JSONシリアライズされたResponse
     * @return jsonRequestをparseして得たインスタンスを返す。
     *         jsonRequestが不正などでparseに失敗した場合はnullを返す。
     */
    public static <T> T valueOf(String jsonRequest, Class<T> clazz) {
        Method valueOfMethod = getValueOfMethod(clazz);
        try {
            if (valueOfMethod == null) {
                return (T) mapper.readValue(jsonRequest, clazz);
            } else {
                return (T) valueOfMethod.invoke(clazz, jsonRequest);
            }
        } catch (JsonParseException e) {
         //   logger.debug("Received JSON format is invalid.");
            return null;
        } catch (JsonMappingException e) {
          //  logger.debug("Received Resource does not conform to the terms.");
          //  logger.debug(e.getMessage());
            return null;
        } catch (Exception e) {
         //   logger.debug(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * JSONシリアライズされたVoをparseしてインスタンスを生成するスタティックファクトリメソッド
     * 
     * @param jsonRequest
     *            JSONシリアライズされたResponse
     * @return jsonRequestをparseして得たインスタンスを返す。
     *         jsonRequestが不正などでparseに失敗した場合はnullを返す。
     */
    public static <T> T valueOfNoRoot(String jsonRequest, Class<T> clazz) {
        try {
            
            return (T) noRootMapper.readValue(jsonRequest, clazz);
            
        } catch (JsonParseException e) {
        //    logger.debug("Received JSON format is invalid.");
            return null;
        } catch (JsonMappingException e) {
        //    logger.debug("Received Resource does not conform to the terms.");
         //   logger.debug(e.getMessage());
            return null;
        } catch (Exception e) {
          //  logger.debug(e.getMessage(), e);
            return null;
        }
    }
    
    public void validate() throws ApplicationException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory(); 
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<BaseJsonVo>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new ApplicationException(Constants.INPUT_PARAM_ERROR);
        }
        //violations.
    }
    
}
