package com.kddi.tmc.pushasg.common.util;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties
@Getter
@Setter
public class ApplicationProperties {
    // private String cseBrokerUrl;
    // private String mzdBrokerUrl;
    // private String inAeId;
    private Map<String, Map<String, String>> brokerTopicMapplingList;
    
    private String tls_keystore_file;
    private String tls_keystore_pass;
    private String tls_truststore_file;
    private String tls_truststore_pass;

//    private Map<String, Map<String, String>> clientBrokerMapping;

    public String getBrokerUrlFromDevUniqId(String devUniqId) {
        
        return getValueFromDevUniqId(devUniqId, "broker-url");

    }
    
    public String getBusinessCdFromDevUniqId(String devUniqId) {
//        Map<String, String> info = brokerTopicMapplingList.get(devUniqId);
//        if (info == null) {
//            return null;
//        }
        
        return getValueFromDevUniqId(devUniqId, "business-cd");

    }
    
    public String getPushTopicFromDevUniqId(String devUniqId) {
//        Map<String, String> info = brokerTopicMapplingList.get(devUniqId);
//        if (info == null) {
//            return null;
//        }
        
        return getValueFromDevUniqId(devUniqId, "push-topic");
    }
    
    public String getSubTopicFromDevUniqId(String devUniqId) {
//        Map<String, String> info = brokerTopicMapplingList.get(devUniqId);
//        if (info == null) {
//            return null;
//        }
        
        return getValueFromDevUniqId(devUniqId, "sub-topic");
    }
    
    public String getCommandTopicFromDevUniqId(String devUniqId) {
        return getValueFromDevUniqId(devUniqId, "command-topic");
    }
  
    public String getRespTopicFromDevUniqId(String devUniqId) {
        return getValueFromDevUniqId(devUniqId, "resp-topic");
    }

    
    public String[] getAppIdFromDevUniqId(String devUniqId) {
//        Map<String, String> info = brokerTopicMapplingList.get(devUniqId);
//        if (info == null) {
//            return null;
//        }
        
        String value = getValueFromDevUniqId(devUniqId, "app-id");
        
        return value == null ? new String[0] : value.split(",");
    }
    
    private String getValueFromDevUniqId(String devUniqId, String name) {
        Map<String, String> info = brokerTopicMapplingList.get(devUniqId);
        if (info == null) {
            info = brokerTopicMapplingList.get("default");
        }
        
        if (info == null) {
            return null;
        }
        
        String value = info.get(name);
        
        if (value != null) {
            value = value.replaceAll("\\{DEV_UNIQ_ID\\}", devUniqId);
        }
        
        return value;
    }
    
    public String[] getSslParamsFromDevUniqId(String devUniqId) {
      
      String value = getValueFromDevUniqId(devUniqId, "ssl-params");
      
      return value == null ? getSslParams() : value.split(",");
  }
    
//    public String getBrokerUrlFromClientId(String clientId) {
//        Map<String, String> info = clientBrokerMapping.get(clientId);
//        if (info == null) {
//            return null;
//        }
//        
//        return info.get("broker-url");
//    }
//    
//    public String getPushServerIdFromClientId(String clientId) {
//        Map<String, String> info = clientBrokerMapping.get(clientId);
//        if (info == null) {
//            return null;
//        }
//        
//        return info.get("push-server-id");
//    }
    
    public String[] getSslParams(){
    	String[] sslParams = new String[4];
    	sslParams[0] = tls_keystore_pass;
    	sslParams[1] = tls_keystore_file;
    	sslParams[2] = tls_truststore_pass;
    	sslParams[3] = tls_truststore_file;
    	
    	return sslParams;
    }
}
