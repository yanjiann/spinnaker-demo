package com.kddi.tmc.pushasg.common.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimContext {
    private String allocBrokerUrl;

    private String reqAllocTopic;

    private String brokerUrl;

    private String pushSrvId;

    // private String brokerSrvId;

    private String devUniqId;
    
//    private String namespace;

    private String businessCd;

    private String clientId;

    private String[] appIds;

    private String[] sslParams;

    private HashSet<String> groups = new HashSet<String>();
    
    private List<GroupListener> groupListeners = new ArrayList<>();

    public void addGroup(String... group) {
        synchronized (groups) {
            ArrayList<String> added = new ArrayList<>();
            
            for (String s : group) {
                if (!groups.contains(s)) {
                    groups.add(s);
                    added.add(s);
                }
            }
            if (!added.isEmpty()) {
                for (GroupListener groupListener : groupListeners) {
                    groupListener.added(new GroupEvent(
                            added.toArray(new String[0]),
                            added.toArray(new String[0]), GroupEventType.ADD));
                }
            }
        }
    }

    public void removeGroup(String... group) {
        synchronized (groups) {
            ArrayList<String> removed = new ArrayList<>();
            
            for (String s : group) {
                if (groups.contains(s)) {
                    groups.remove(s);
                    removed.add(s);
                }
            }
            
            if (!removed.isEmpty()) {
                for (GroupListener groupListener : groupListeners) {
                    groupListener.removed(
                            new GroupEvent(removed.toArray(new String[0]),
                                    removed.toArray(new String[0]),
                                    GroupEventType.REMOVE));
                }
            }
        }
    }
    
    public void changeGroup(String... group) {
        
        synchronized (groups) {
            String[] oldGroups = groups.toArray(new String[0]);
            groups.clear();
            groups.addAll(Arrays.asList(group));

            for (GroupListener groupListener : groupListeners) {
                groupListener.changed(new GroupEvent(oldGroups, group, GroupEventType.CHANGE));
            }
        }
    }

    public String[] getAllGroup() {
        return groups.toArray(new String[0]);
    }

    public boolean hasGroup(String group) {
        return groups.contains(group);
    }

    private static final Map<String, SimContext> devContexts = new HashMap<>();

    public static void setDevContext(String key, SimContext context) {
        synchronized (devContexts) {
            devContexts.put(key, context);
        }
    }

    public static SimContext getDevContext(String key) {

        return devContexts.get(key);

    }
    
    public void addGroupListener(GroupListener listener) {
        synchronized(groupListeners) {
            groupListeners.add(listener);
        }
    }
    
    public void removeGroupListener(GroupListener listener) {
        synchronized(groupListeners) {
            groupListeners.remove(listener);
        }
    }
    
    public static enum GroupEventType {
        ADD, REMOVE, CHANGE
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupEvent {
        private String[] oldGroups;
        private String[] newGroups;
        private GroupEventType type;
        
    }
    
    public static interface GroupListener {
        void changed(GroupEvent event);
        
        void added(GroupEvent event);
        
        void removed(GroupEvent event);
    }
    
    

}
