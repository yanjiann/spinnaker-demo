package com.kddi.tmc.pushasg.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.kddi.tmc.pushasg.common.vo.ApplicationSession;

public class SessionManagement {

    private static final Map<String, ApplicationSession> sessionInfos = new HashMap<String, ApplicationSession>();
    
    public static String createSessionId() {
        return UUID.randomUUID().toString();
    }
    
    public static void saveSessionId(String sessionId, ApplicationSession value) {
        synchronized(sessionInfos) {
            sessionInfos.put(sessionId, value);
        }
    }
    
    public static void removeSessionId(String sessionId) {
        synchronized (sessionInfos) {
            sessionInfos.remove(sessionId);
        }
    }
    
    public static ApplicationSession getSession(String sessionId) {
        return sessionInfos.get(sessionId);
    }
}
