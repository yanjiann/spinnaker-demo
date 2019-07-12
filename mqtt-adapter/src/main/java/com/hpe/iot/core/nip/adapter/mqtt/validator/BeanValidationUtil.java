package com.hpe.iot.core.nip.adapter.mqtt.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.iot.core.nip.adapter.mqtt.exception.ApplicationException;
import com.hpe.iot.core.nip.adapter.mqtt.utils.MessageUtil;
import com.hpe.iot.core.nip.adapter.mqtt.utils.StringUtil;

public class BeanValidationUtil {

	
	public static <T> T validateMqttRequest(String reqBody, Class<T> tClass) throws ApplicationException{
		
		ObjectMapper mapper = new ObjectMapper();
		
		//if unknown properties encounter throw com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);  
	
		T punS401Request = null;
		List<String> errorMsgs = new ArrayList<String>();
		
		//check unrecognizedPropertyException
		try {
			punS401Request = mapper.readValue(reqBody, tClass);
		} catch (JsonParseException e) {
			
//			logger.writeAppLog(MessageUtil.MSGID_UNKOWN_REQ);
			throw new ApplicationException(Constants.REQUEST_NOT_SUPPORTED);
			
		} catch(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
			
			//log the unrecoginizedProperty error and continue parsing without throw Exception        
	        String unrecognizedProperty = StringUtil.getUnnecessaryField(e.getPathReference());
			errorMsgs.add(MessageUtil.MSGID_PARAM_ERROR_UNNECESSARY+":"+unrecognizedProperty);
//			logger.writeAppLog(MessageUtil.MSGID_PARAM_ERROR_UNNECESSARY, unrecognizedProperty);
			
		} catch (JsonMappingException e) {
			
			 String unrecognizedProperty = StringUtil.getUnnecessaryField(e.getPathReference());
//			logger.writeAppLog(MessageUtil.MSGID_PARAM_ERROR_STYLE, unrecognizedProperty);
			throw new ApplicationException(Constants.INPUT_PARAM_ERROR);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//continue parsing the request by ignore this
		ObjectMapper mapper2 = new ObjectMapper();
		mapper2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  
		try {
			punS401Request = mapper2.readValue(reqBody, tClass);
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
	    // validation not null & length for each property
		List<String> msgs = new ArrayList<String>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory(); 
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(punS401Request);
        if (!violations.isEmpty()) {
            for(ConstraintViolation<T> violation : violations) {
                msgs.add(violation.getMessage());              
            }           
        }
        
	    if (!msgs.isEmpty()) {
	        for (String msg : msgs) {
	            String[] msgInfos = msg.split(":");
	            String msgId = msgInfos[0];
	
	            Object[] msgArgs = null;
	            if (msgInfos.length > 1) {
	                msgArgs = new String[msgInfos.length - 1];
	                System.arraycopy(msgInfos, 1, msgArgs, 0, msgArgs.length);
	            } else {
	                msgArgs = new String[0];
	            }
	            
//	            logger.writeAppLog(msgId, msgArgs);
	        }	        		            
	    }
	    
	    if(!msgs.isEmpty() || !errorMsgs.isEmpty()){
	    	throw new ApplicationException(Constants.INPUT_PARAM_ERROR);
	    }

	    return punS401Request;
	}
	
	public static <T> T validateHttpRequest(String reqBody, Class<T> tClass) throws ApplicationException{
		
		ObjectMapper mapper = new ObjectMapper();
		
		//if unknown properties encounter throw com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);  
	
		T punS401Request = null;
		List<String> errorMsgs = new ArrayList<String>();
		//check unrecognizedPropertyException
		try {
			punS401Request = mapper.readValue(reqBody, tClass);
		} catch (JsonParseException e) {
			
//			logger.writeAppLog(MessageUtil.MSGID_UNKOWN_REQ);
			throw new ApplicationException(Constants.REQUEST_NOT_SUPPORTED, MessageUtil.getMessage(MessageUtil.MSGID_UNKOWN_REQ));
			
		} catch(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
			
			//log the unrecoginizedProperty error
			 String unrecognizedProperty = StringUtil.getUnnecessaryField(e.getPathReference());
//			logger.writeAppLog(MessageUtil.MSGID_PARAM_ERROR_UNNECESSARY, unrecognizedProperty);
			throw new ApplicationException(Constants.INPUT_PARAM_ERROR, MessageUtil.getMessage(MessageUtil.MSGID_PARAM_ERROR_UNNECESSARY, new Object[]{unrecognizedProperty}));

		} catch (JsonMappingException e) {
			 String unrecognizedProperty = StringUtil.getUnnecessaryField(e.getPathReference());
//			logger.writeAppLog(MessageUtil.MSGID_PARAM_ERROR_UNNECESSARY, e.getPathReference());
			throw new ApplicationException(Constants.INPUT_PARAM_ERROR, MessageUtil.getMessage(MessageUtil.MSGID_PARAM_ERROR_STYLE, new Object[]{unrecognizedProperty}));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		//continue parsing the request by ignore this
		ObjectMapper mapper2 = new ObjectMapper();
		mapper2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  
		try {
			punS401Request = mapper2.readValue(reqBody, tClass);
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	    // validation not null & length for each property
		List<String> msgs = new ArrayList<String>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory(); 
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(punS401Request);
		String firstErrorMsg = null;
        
        if (!violations.isEmpty()) {
            for(ConstraintViolation<T> violation : violations) {
                msgs.add(violation.getMessage());              
            }           
        }
        
	    if (!msgs.isEmpty()) {

	        for (String msg : msgs) {
	            String[] msgInfos = msg.split(":");
	            String msgId = msgInfos[0];
	
	            Object[] msgArgs = null;
	            if (msgInfos.length > 1) {
	                msgArgs = new String[msgInfos.length - 1];
	                System.arraycopy(msgInfos, 1, msgArgs, 0, msgArgs.length);
	            } else {
	                msgArgs = new String[0];
	            }
	            
//	            logger.writeAppLog(msgId, msgArgs);
	            
	            if(firstErrorMsg == null){
	            	firstErrorMsg = MessageUtil.getMessage(msgId, msgArgs);
	            }
	        }	        		            
	    }
	    
	    if(!msgs.isEmpty() || !errorMsgs.isEmpty()){
	    	throw new ApplicationException(Constants.INPUT_PARAM_ERROR, firstErrorMsg);
	    }

	    return punS401Request;
	}
    
}
