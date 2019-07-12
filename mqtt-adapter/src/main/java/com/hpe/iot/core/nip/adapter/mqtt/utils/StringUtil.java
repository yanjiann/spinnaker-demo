package com.hpe.iot.core.nip.adapter.mqtt.utils;

import java.io.UnsupportedEncodingException;

public final class StringUtil {

    public static final String ENC = "UTF-8";
    
    public static String toString(byte[] contents) {
        try {
            return new String(contents, ENC);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static byte[] toBytes(String contents) {
        try {
            return contents.getBytes(ENC);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static boolean isNotEmpty(String val) {
        
        return !isEmpty(val);
    }
    
    public static boolean isEmpty(String val) {
        if(val == null) {
            return true;
        }
        
        return val.trim().isEmpty();
    }
    
    //Sample input/out
    //input: com.hpe.iot.dc.message.bean.PunS301Request[\"retewtet\"]"
    //output: retewtet
    public static String getUnnecessaryField(String referencePath){
    	String resultStr = "";
    	 String[] errItem = referencePath.split("\"");
    	 if(errItem != null && errItem.length > 1){
    		 resultStr = errItem[1];
    	 }	
    	 return resultStr;
    }
}
