package com.kddi.tmc.pushasg.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSession {

    private String sendTopic;
    
    private String subscribeTopic;
    
//    private String brokerUrl;
//    
//    private String pushSrvId;
//    
//    private String devUniqId;
//    
//    private String clientId;
//    
//    private String group;
    
    private Object sendObject;
    
    private Object recieveObject;
    
}
