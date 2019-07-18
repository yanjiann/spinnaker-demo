package com.kddi.tmc.pushasg.common.vo;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiiContext {
    private String brokerUrl;
    private String devUniqId;
    private String clientId;
    private String[] sslParams;
    private String respTopic;

    private static final Map<String, MiiContext> devContexts = new HashMap<>();
    public static void setDevContext(String key, MiiContext context) {
        synchronized (devContexts) {
            devContexts.put(key, context);
        }
    }
    public static MiiContext getDevContext(String key) {

        return devContexts.get(key);

    }
}
